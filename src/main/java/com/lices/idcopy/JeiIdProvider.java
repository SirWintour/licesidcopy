package com.lices.idcopy;

import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

public class JeiIdProvider {

    public static String getIdFromJeiOverlay() {
        try {
            Object runtime = JeiIntegration.getRuntime();
            if (runtime == null) {
                LicesIDCopy.LOGGER.debug("JEI runtime não disponível");
                return null;
            }

            // Try each of the 3 types of overlays.
            String[] methods = {"getIngredientListOverlay", "getBookmarkOverlay", "getRecipesGui"};
            for (String method : methods) {
                Object overlay = invokeMethod(runtime, method);
                if (overlay == null) {
                    LicesIDCopy.LOGGER.debug("Overlay {} não disponível", method);
                    continue;
                }

                Object hoveredIngredient = getHoveredIngredientFromOverlay(overlay);

                if (hoveredIngredient != null) {
                    LicesIDCopy.LOGGER.debug("Ingrediente hovereado: {}", hoveredIngredient.getClass().getName());
                    return extractIdFromTypedIngredient(hoveredIngredient);
                }
            }
            // No results, so just exit.
            return null;

        } catch (Exception e) {
            LicesIDCopy.LOGGER.debug("Erro ao acessar JEI overlay", e);
        }

        return null;
    }

    private static Object getHoveredIngredientFromOverlay(Object overlay) {
        try {
            Class<?> clazz = overlay.getClass();

            Object result = searchFieldsForHoveredIngredient(overlay, clazz);
            if (result != null) {
                return result;
            }

            Method method = findMethod(clazz, "getIngredientUnderMouse", "getHoveredIngredient", "getElementUnderMouse");
            if (method != null) {
                method.setAccessible(true);
                Object methodResult = method.invoke(overlay);

                if (methodResult != null) {
                    LicesIDCopy.LOGGER.debug("Resultado do método {}: {}", method.getName(), methodResult.getClass().getName());

                    if (methodResult instanceof Optional<?> optional) {
                        if (optional.isPresent()) {
                            Object unwrapped = optional.get();
                            LicesIDCopy.LOGGER.debug("Optional desembrulhado: {}", unwrapped.getClass().getName());
                            return unwrapped;
                        } else {
                            LicesIDCopy.LOGGER.debug("Optional está vazio");
                            return null;
                        }
                    }

                    return methodResult;
                }
            }

        } catch (Exception e) {
            LicesIDCopy.LOGGER.debug("Erro ao buscar ingrediente hovereado", e);
        }

        return null;
    }

    private static Object searchFieldsForHoveredIngredient(Object overlay, Class<?> clazz) {
        Class<?> current = clazz;

        while (current != null && current != Object.class) {
            Field[] fields = current.getDeclaredFields();
            LicesIDCopy.LOGGER.debug("Procurando em classe: {} ({} campos)", current.getSimpleName(), fields.length);

            for (Field field : fields) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(overlay);

                    if (value == null) continue;

                    String fieldName = field.getName().toLowerCase();
                    String className = value.getClass().getName();

                    if (fieldName.contains("hovered") || fieldName.contains("hover") ||
                            fieldName.contains("focus") || fieldName.contains("mouse") ||
                            fieldName.contains("ingredient") || fieldName.contains("element")) {
                        LicesIDCopy.LOGGER.debug("  Campo: {} = {}", field.getName(), className);
                    }

                    if (className.contains("TypedIngredient")) {
                        LicesIDCopy.LOGGER.info("✓ TypedIngredient encontrado no campo: {}", field.getName());
                        return value;
                    }

                    if (value instanceof Optional<?> optional && optional.isPresent()) {
                        Object optValue = optional.get();
                        if (optValue.getClass().getName().contains("TypedIngredient")) {
                            LicesIDCopy.LOGGER.info("✓ TypedIngredient encontrado em Optional: {}", field.getName());
                            return optValue;
                        }
                    }

                } catch (Exception e) {
                }
            }
            current = current.getSuperclass();
        }

        return null;
    }

    private static String extractIdFromTypedIngredient(Object typedIngredient) {
        try {
            Object ingredient = invokeMethod(typedIngredient, "getIngredient");

            if (ingredient == null) {
                LicesIDCopy.LOGGER.warn("getIngredient() retornou null");
                return null;
            }

            String className = ingredient.getClass().getName();
            LicesIDCopy.LOGGER.debug("Ingredient extraído: {}", className);

            if (className.contains("FluidStack")) {
                return extractIdFromFluidStack(ingredient);
            }

            if (className.contains("ChemicalStack")) {
                return extractIdFromChemicalStack(ingredient);
            }

            if (ingredient instanceof ItemStack itemStack && !itemStack.isEmpty()) {
                return getItemId(itemStack);
            }

            LicesIDCopy.LOGGER.warn("Tipo de ingrediente desconhecido: {}", className);

        } catch (Exception e) {
            LicesIDCopy.LOGGER.error("Erro ao extrair TypedIngredient", e);
        }

        return null;
    }

    private static String extractIdFromFluidStack(Object fluidStack) {
        try {
            LicesIDCopy.LOGGER.debug("Extraindo FluidStack: {}", fluidStack);

            Object fluid = invokeMethod(fluidStack, "getFluid");

            if (fluid == null) {
                LicesIDCopy.LOGGER.warn("getFluid() retornou null");
                return null;
            }

            LicesIDCopy.LOGGER.debug("Fluid obtido: {}", fluid.getClass().getName());

            return extractIdFromFluid(fluid);

        } catch (Exception e) {
            LicesIDCopy.LOGGER.error("Erro ao extrair FluidStack", e);
        }

        return null;
    }

    private static String extractIdFromFluid(Object fluid) {
        try {
            String className = fluid.getClass().getName();

            if (className.contains("Holder")) {
                LicesIDCopy.LOGGER.debug("Fluid é Holder direto");
                return extractIdFromHolder(fluid);
            }

            Field holderField = findField(fluid.getClass(), "builtInRegistryHolder");
            if (holderField != null) {
                holderField.setAccessible(true);
                Object holder = holderField.get(fluid);

                if (holder != null) {
                    LicesIDCopy.LOGGER.debug("Holder obtido do campo builtInRegistryHolder");
                    return extractIdFromHolder(holder);
                } else {
                    LicesIDCopy.LOGGER.warn("builtInRegistryHolder é null");
                }
            } else {
                LicesIDCopy.LOGGER.warn("Campo builtInRegistryHolder não encontrado em {}", className);

                LicesIDCopy.LOGGER.debug("Campos disponíveis em Fluid:");
                for (Field f : fluid.getClass().getDeclaredFields()) {
                    LicesIDCopy.LOGGER.debug("  - {}", f.getName());
                }
            }

        } catch (Exception e) {
            LicesIDCopy.LOGGER.error("Erro ao extrair Fluid", e);
        }

        return null;
    }

    private static String extractIdFromChemicalStack(Object chemicalStack) {
        try {
            LicesIDCopy.LOGGER.debug("Extraindo ChemicalStack: {}", chemicalStack);

            Object chemical = invokeMethod(chemicalStack, "getChemical");

            if (chemical == null) {
                LicesIDCopy.LOGGER.warn("getChemical() retornou null");
                return null;
            }

            LicesIDCopy.LOGGER.debug("Chemical obtido: {}", chemical.getClass().getName());

            return extractIdFromChemical(chemical);

        } catch (Exception e) {
            LicesIDCopy.LOGGER.error("Erro ao extrair ChemicalStack", e);
        }

        return null;
    }

    private static String extractIdFromChemical(Object chemical) {
        try {
            String className = chemical.getClass().getName();

            if (className.contains("Holder")) {
                LicesIDCopy.LOGGER.debug("Chemical é Holder direto");
                return extractIdFromHolder(chemical);
            }

            Field holderField = findField(chemical.getClass(), "builtInRegistryHolder");
            if (holderField != null) {
                holderField.setAccessible(true);
                Object holder = holderField.get(chemical);

                if (holder != null) {
                    LicesIDCopy.LOGGER.debug("Holder obtido do campo builtInRegistryHolder");
                    return extractIdFromHolder(holder);
                } else {
                    LicesIDCopy.LOGGER.warn("builtInRegistryHolder é null");
                }
            } else {
                LicesIDCopy.LOGGER.warn("Campo builtInRegistryHolder não encontrado em {}", className);

                LicesIDCopy.LOGGER.debug("Campos disponíveis em Fluid:");
                for (Field f : chemical.getClass().getDeclaredFields()) {
                    LicesIDCopy.LOGGER.debug("  - {}", f.getName());
                }
            }

        } catch (Exception e) {
            LicesIDCopy.LOGGER.error("Erro ao extrair Chemical", e);
        }

        return null;
    }

    private static String extractIdFromHolder(Object holder) {
        try {
            Object key = invokeMethod(holder, "key");

            if (key != null) {
                Object location = invokeMethod(key, "location");

                if (location != null) {
                    String id = location.toString();
                    LicesIDCopy.LOGGER.debug("ResourceLocation: {}", id);
                    return id;
                }
            }
        } catch (Exception e) {
            LicesIDCopy.LOGGER.error("Erro ao extrair Holder", e);
        }

        return null;
    }

    private static String getItemId(ItemStack itemStack) {
        return itemStack.getItem().builtInRegistryHolder().key().location().toString();
    }

    private static Object invokeMethod(Object obj, String methodName) throws Exception {
        Method method = obj.getClass().getMethod(methodName);
        method.setAccessible(true);
        return method.invoke(obj);
    }

    private static Method findMethod(Class<?> clazz, String... methodNames) {
        for (String name : methodNames) {
            try {
                return clazz.getDeclaredMethod(name);
            } catch (NoSuchMethodException e) {
            }
        }
        return null;
    }

    private static Field findField(Class<?> clazz, String... fieldNames) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (String name : fieldNames) {
                try {
                    return current.getDeclaredField(name);
                } catch (NoSuchFieldException e) {
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }
}

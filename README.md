# Item ID Copy

Um mod simples e prático para Minecraft que permite copiar facilmente o ID de qualquer item diretamente do seu inventário ou do JEI.

## Descrição

Lice's ID Copy foi desenvolvido para facilitar a vida de modders e builders que frequentemente precisam de IDs de itens. Ao invés de procurar em wikis ou fazendo buscas manuais, agora você pode copiar o ID com um simples atalho de teclado.

## Funcionalidades

- Cópia rápida de IDs de itens com Ctrl+C
- Funciona tanto no inventário do Minecraft quanto no JEI
- Feedback visual com mensagem no chat ao copiar
- Interface limpa e sem distrações

## Como Usar

1. Abra seu inventário ou o JEI
2. Passe o mouse sobre qualquer item
3. Pressione Ctrl+C para copiar o ID do item
4. O ID será copiado para sua área de transferência e uma mensagem de confirmação aparecerá

Exemplo de ID copiado: `minecraft:diamond_pickaxe` ou `modname:custom_item`

## Como Funciona

O mod utiliza dois mecanismos para capturar o ID dos itens:

### No Inventário
O mod escuta o evento de renderização de tooltip do Minecraft. Quando um item está sendo renderizado (tooltip visível), o mod captura automaticamente o ItemStack e armazena a referência. Ao pressionar Ctrl+C, o ID é extraído do ItemStack através da API de registro do Minecraft.

### No JEI
O mod utiliza o evento `RenderTooltipEvent.Pre` do NeoForge para capturar o ItemStack quando o tooltip está sendo renderizado. Como o JEI também renderiza tooltips de itens, o mesmo sistema funciona automaticamente. Adicionalmente, há um sistema de fallback que usa reflexão para buscar o item diretamente nos campos da interface do JEI, garantindo compatibilidade mesmo se o tooltip não estiver visível.

## Instalação

1. Coloque o arquivo .jar do mod na pasta `mods` do seu Minecraft
2. Abra o Minecraft e pronto, o mod estará ativo

## Autor

Licescutie

## Licença

Este mod é fornecido sob a mesma licença do NeoForge. Consulte o arquivo TEMPLATE_LICENSE.txt para mais informações.

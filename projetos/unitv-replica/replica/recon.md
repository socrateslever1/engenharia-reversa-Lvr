# Replica — inventário inicial do UniTVnet (Android)

Data: 2026-11-02. Objetivo: reconstrução independente de UX e fluxos, com verificação explícita de paridade. Esta NÃO é uma modificação dos binários originais.

## Fonte de verdade e limites

| Origem | Evidência | Limite |
|---|---|---|
| APK Mobile 1.11.0 fornecido pelo usuário | SHA256 668d4697bb90fc8b6a0fcf36b26c9f9adb2650ffd63ba845e6f9b806476fb147 | Binário não reproduzido no novo projeto; nenhum endpoint privado será reaproveitado |
| APK TV 1.10.0 fornecido pelo usuário | SHA256 a07c2538df7505263d7a92f76298dac891fd3755db391b9282f7712f0cec8067 | Tela TV ainda não observada em execução |
| Captura Mobile fornecida pelo usuário | Arquivo de referência local `replica/screens/mobile-home-original.png` **não publicado** | Confirma uma tela e um estado; não prova fluxos de reprodução, login ou pesquisa |

## Fluxo central original

Abrir aplicativo → navegar entre Recomendações/Filmes/Séries/Animes ou TV → selecionar item → abrir player → reproduzir por serviço autorizado. **Ainda não reproduzido.**

## Telas observadas e candidatas

| ID | Tela | Fonte | Evidência na interface | Estado de verificação |
|---|---|---|---|---|
| S01 | Início Mobile | captura original | marca, busca, filtros, categorias, carrossel, ao vivo, jogos, menu inferior | **observada** |
| S02 | TV Mobile | botão inferior na captura | destino TV | **não observado** |
| S03 | Novidades | botão inferior na captura | destino Novidades | **não observado** |
| S04 | Perfil | botão inferior na captura | destino Perfil | **não observado** |
| S05 | Pesquisa e filtros | busca/filtro na captura | entrada de consulta | **não observado em execução** |
| S06 | Player Mobile | não visível na captura | indicado como função pelo usuário | **não observado** |
| S07 | Início Android TV | APK original recebido | comportamento não capturado em tela | **não observado em execução** |
| S08 | Player TV | APK original recebido | comportamento não capturado em tela | **não observado em execução** |

## Fluxos — hipóteses aguardando evidência

F01 Navegação Mobile: S01 → S02/S03/S04. A tela de S01 é observada, as transições ainda precisam de captura.
F02 Categorias: S01 → Recomendações/Filmes/Séries/Animes. Somente cabeçalhos observados.
F03 Reprodução: S01 → S06. Não afirmar conclusão enquanto não houver fonte autorizada.
F04 TV: S07 → S08. Não afirmar conclusão sem comparação com original em TV.

## Componentes visuais observados em S01

Top bar; logo; campo busca com filtro; abas horizontais de categoria; área de banner; pager de indicadores; título de seção; cartões horizontais de transmissão ao vivo; cartões esportivos; tab bar inferior com quatro destinos.

## Modelo de dados mínimo proposto (NÃO confirmado)

- Content: id, title, category, poster, playableRef (provisório). **Sem fonte de dados autorizada**.
- LiveChannel: id, title, cover, streamRef (provisório). **Sem fonte de dados autorizada**.
- SportsEvent: id, competitorA, competitorB, startTime, status (provisório). **Sem fonte de dados autorizada**.

## Fora de escopo

Extrair ou copiar chaves, marcas/logotipos, filmes, imagens de capa, base de assinantes, APIs privadas e proteção DRM. Não simular acesso à conta do usuário. Não incluir backend falso nem canais fictícios. Não publicar APKs originais.

## Tamanho e risco

XL: reconstrução de telas e UX Android/TV depende de capturas completas dos fluxos; paridade de catálogo, sessão e reprodução depende de interface autorizada do serviço. Estudo de S01 e navegação visual é um pequeno incremento independente, sem equivalência de produto.

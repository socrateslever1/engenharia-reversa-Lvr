# Evidências extraídas dos APKs UniTVnet — 2026-11-01

Análise estática read-only dos APKs recebidos. Scripts completos de mapeamento ARSC/AXML, testes e arquivos de saída foram gerados no pacote técnico da conversa; não se publica código binário proprietário aqui.

## Identidade e integridade
| Referência | TV | Mobile |
|---|---|---|
| Arquivo | `UniTVnet_1.10.0.apk` | `UniTVnet_1.11.0.apk` |
| Pacote | `com.unitvnet.tvod` | `com.unitvnet.mobs` |
| SHA-256 | `a07c2538df7505263d7a92f76298dac891fd3755db391b9282f7712f0cec8067` | `668d4697bb90fc8b6a0fcf36b26c9f9adb2650ffd63ba845e6f9b806476fb147` |
| CRC ZIP | OK | OK |
| Wrapper | `com.secneo.apkwrapper.AW` | `com.secneo.apkwrapper.AW` |
| Recursos identificados via ARSC | 7.407 entradas | 9.703 entradas |

## Mapeamento da interface Mobile real

| Tela da captura | Layout original | Hierarquia recuperada |
|---|---|---|
| Shell principal e navegação inferior | `aty_main` | `AutoRelativeLayout`, `NoScrollViewPager`, `LottieTabLayout` (`mainAtyTab`) |
| Página inicial | `frag_home` | `SwipeRefreshLayout`, `RecyclerView` (`mHomeRV`) |
| Banner rotativo | `adapter_home_banner_new` | `BannerViewPager`, `IndicatorView` |
| Transmissões ao vivo | `adapter_home_live_stream` | `RecyclerView` (`rvStream`) |
| Jogos e esportes | `adapter_home_match` | `RecyclerView` (`rvMatchList`) |
| Itens de navegação inferior | `layout_tab_bottom` | `tv_tab_title`, `iv_tab_icon` |
| Player de VOD | `aty_play` | `TitanPlayerController` (`mVodPlayer`) |
| Superfície de VOD | `layout_titan_vod_player_controller` | `TitanVODView` (`mVideoViewVod`) |
| Controles de vídeo | `layout_vod_controller_panel` | play, seekbar, legendas, fullscreen, cast |
| Reprodução ao vivo | `frag_live_play` | `TitanVideoView` (`mVideoViewLive`) |

A tela enviada tem seções e conteúdos dinâmicos. Os identificadores de layouts comprovam a estrutura, não os dados de catálogo nem seus serviços.

## Mapeamento TV
- `activity_home` → `ViewPager2`, `VerticalGridView`, `NotificationBar`.
- `activity_vod_home` → `HorizontalGridView`, `CustomViewPager`.
- `fragment_home_vod` → `VerticalGridView`, `mListView`.
- `fragment_home_live` → `mLvChannelList`, `mFlMainLive`, listas de eventos.
- `fragment_vod_player` → `MFInfoBar`, controles de player.
- `layout_live_player_controller` → `LivePlayerBufferView`, status de playback.

## Bloqueio concreto
Os layouts AXML referem classes de UI, mas não contém seus métodos Java/Kotlin. O código funcional está empacotado pelo Secneo. Não foram comprovados os ciclos de vida dos players, APIs internas, autenticação, deeplinks ou reprodução fora do cliente original. Modificar apenas o manifesto para PiP é insuficiente; re-assinatura não conserva a assinatura privada do fornecedor.

## Critérios de saída
(1) Recuperar interfaces utilizáveis do player original; (2) comparar em aparelho os fluxos de login, catálogo, navegação e reprodução; (3) realizar fusão funcional TV/Mobile; (4) **só então** acrescentar PiP Mobile, MediaSession e Watch Next. Nenhum APK deve ser apresentado como equivalente antes dessas verificações.

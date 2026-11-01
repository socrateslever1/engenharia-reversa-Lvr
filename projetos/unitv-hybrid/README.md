# UniTV Hybrid — engenharia reversa e integração Android

**Este não é um APK reconstruído do UniTVnet.** É uma biblioteca original de integração Android, ainda sem ligação ao player protegido, desenvolvida a partir da comparação estática dos APKs TV 1.10.0 e Mobile 1.11.0.

## Implementado no código
- Detecção TV/Mobile com opção manual persistente.
- MediaSession nativa para play/pause por controle/fone e pausa por AUDIO_BECOMING_NOISY.
- PiP Mobile Android 8+, automático no Android 12+ enquanto reproduz vídeo.
- Publicação Watch Next para VOD parcialmente reproduzido, com atualização idempotente e remoção.
- Políticas unit-testable; workflow Android para gerar AAR de biblioteca.

## Requisitos para incorporar no UniTVnet

Integração legítima ao player por `PlaybackPort`, eventos reais de reprodução, metadata e deep links internos. O código cliente está empacotado com Secneo; nada aqui contorna proteção, autenticação ou DRM. A experiência e conta originais NÃO foram testadas neste código. Não há APK pronto.

Use somente o player original de modo autorizado. Não criar MediaSession duplicada. O PiP exige declaração `supportsPictureInPicture` na Activity de vídeo. Google TV exige certificação para mostrar Continue Watching.

## Build

`gradle :integration:testDebugUnitTest :integration:assembleDebug`

JDK 17 + Android SDK 35 + Gradle 8.11.1. Artifact é um AAR, não um APK. Os APKs originais NÃO são versionados.

## Hashes das referências

TV 1.10.0: `a07c2538df7505263d7a92f76298dac891fd3755db391b9282f7712f0cec8067`

Mobile 1.11.0: `668d4697bb90fc8b6a0fcf36b26c9f9adb2650ffd63ba845e6f9b806476fb147`

**Fora do escopo:** Recentes e proteção contra fechamento acidental (pertencem a outro projeto), clonagem de serviços, servidores, conta ou painel de assinantes.

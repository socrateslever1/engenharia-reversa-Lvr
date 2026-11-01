# Escopo e definição de pronto

## Objetivo
Cliente UniTVnet adaptativo TV/Mobile, preservando a experiência original, serviços e conta legítima, com melhorias de integração Android.

## Solicitações dentro do escopo
- Play/pause por fone, Bluetooth e controle remoto na própria sessão de mídia.
- Continuar assistindo no launcher de Android TV para conteúdos VOD efetivamente vistos; Google TV sujeito à certificação.
- Detecção automática TV/Mobile; alternativa manual persistente.
- PiP Mobile para vídeo em reprodução: Android 8+, auto-enter Android 12+.

## Explicitamente fora
- Recentes e proteção de saídas acidentais: usuário esclareceu que pertencem a outro projeto.
- Reimplementação do backend, gerenciador de assinaturas, DRM ou credenciais.

## Restrições de integração
- Código remoto atual é somente uma biblioteca de integração, não um APK UniTVnet.
- `PlaybackPort` requer eventos reais, posição, duração e comandos de player legitimamente acessíveis.
- PiP depende de declaração na Activity de vídeo e da integração com seu ciclo de vida.
- Watch Next depende de launcher/Google TV e de deep link verificado.
- Não substituir o app original nem assumir que recompilar a biblioteca preserva sua autenticação.

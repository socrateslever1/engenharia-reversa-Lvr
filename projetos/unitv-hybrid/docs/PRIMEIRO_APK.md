# Primeiro APK de teste — versão 0.1.0-alpha

Este pacote instalável é um **protótipo original**, não é o UniTVnet Mobile/TV recompilado nem reproduz a conta do usuário.

## O que funciona para validação
- Uma Activity que aparece tanto no launcher Mobile quanto no Android TV.
- Detecção de TV/Mobile com opção manual de modo.
- Player Media3 para MP4 e HLS HTTPS de fonte autorizada, com demonstração pública.
- MediaSession Android para controles play/pause de fones e controles.
- PiP em Android 8+ no modo Mobile, incluindo auto-enter quando compatível.

## O que NÃO funciona neste marco
- Catálogo do UniTVnet, login, conta vitalícia, servidores ou reprodução protegida do serviço original.
- Publicação real do histórico no Watch Next (a biblioteca está pronta, mas requer IDs de conteúdo e deep links provenientes de uma integração autorizada).
- Testes instrumentados reais em celular e TV (apenas compilação e testes unitários).

## Instalação
Baixar `unitv-hybrid-alpha-apk` no artefato do workflow Android. Extrair o ZIP para obter `app-debug.apk`. A assinatura automática é de debug, não uma assinatura de produção estável; não atualizar versões posteriores por cima sem chave estável.

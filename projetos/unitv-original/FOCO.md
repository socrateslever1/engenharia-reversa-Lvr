# Regra de foco — engenharia reversa do UniTVnet original

**Fonte de verdade:** `UniTVnet_1.11.0.apk` (Mobile) e `UniTVnet_1.10.0.apk` (TV).

**Meta:** preservar fielmente interfaces, navegação, catálogo, autenticação e reprodução originais, observados no dispositivo, e unificar as interfaces em um APK Android adaptativo. **Apenas depois**: PiP Mobile, MediaSession (fones e controle remoto), Watch Next na TV.

## Restrições vinculantes
1. O APK Mobile original é a referência funcional e visual principal, incluindo a captura fornecida pelo usuário.
2. Nenhum componente de UI ou fluxo pode ser inventado para parecer UniTVnet. Cada implementação deve referenciar uma tela/estado original observável.
3. Não substituir o aplicativo por player genérico, formulário de URL, vídeo de demonstração, catálogo fictício ou identidade similar.
4. As duas edições originais são imutáveis. Identificar por SHA-256; nunca versionar APKs proprietários num repositório público.
5. Separar evidência **extraída**, **inferência** e **funcionamento realmente executado e testado**.
6. Não anunciar APK reconstruído sem comprovar equivalência da autenticação, catálogo, navegação e reprodução em Mobile e TV.
7. Não mexer em backend, painel de assinantes, licença, DRM, contas ou outros controles. Não publicar URLs, tokens e credenciais.
8. Recentes e tratamento de saídas acidentais são de outro projeto, conforme correção expressa do usuário.
9. Aplicar as skills Superpowers de depuração sistemática, execução disciplinada e verificação, em branch própria; sem TinyFish.
10. **Gate de entrega:** teste lado a lado com o original, preservação de conta e serviços; atualizações posteriores exigem assinatura controlada.

## Estado atual
- Originais auditados estaticamente; hierarquia do Mobile e TV recuperada dos arquivos `resources.arsc` e AXML.
- Código funcional protegido por Secneo não recuperado; integração com serviço original não comprovada.
- APK híbrido com equivalência funcional ainda NÃO produzido.
- O projeto `unitv-hybrid` anterior é um protótipo reprovado; **não reutilizar** como cliente original.

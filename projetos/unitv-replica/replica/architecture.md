# Arquitetura mínima da réplica — início independente

- Plataforma: Android Java 17, `Activity` e componentes UI nativos; Gradle 8.11.1, Android Gradle Plugin 8.7.3, SDK 35, minSDK 23.
- **Sem backend e sem permissão INTERNET**, pois não há API de catálogo autorizada. Isso impede falsos conteúdos e vazamento acidental de credenciais.
- `ModeResolver`: escolha independente de densidade/resolução e com preferência manual persistida.
- `ScreenState`: rotas e categorias enumeradas, conteúdo explicitamente vazio.
- `MainActivity`: entrada Mobile e TV, navegação local e painel de diagnóstico. Sem tela ou vídeo fictícios.
- O protótipo de player UniTV Hybrid rejeitado não é usado como código, fonte de imagens, manifesto ou dependência.

## Gate para a próxima integração

É necessário obter catálogo e reprodução por via documentada e autorizada, com ciclo de vida do player e operações de sessão testadas. Enquanto isso, **não liberar um APK como substituto funcional do UniTVnet**. A primeira compilação, se produzida, será classificada como ferramenta de validação de interface/navegação.

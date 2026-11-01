# Referências imutáveis da engenharia reversa

| Evidência | TV | Mobile |
|---|---|---|
| APK | UniTVnet_1.10.0.apk | UniTVnet_1.11.0.apk |
| SHA-256 | `a07c2538df7505263d7a92f76298dac891fd3755db391b9282f7712f0cec8067` | `668d4697bb90fc8b6a0fcf36b26c9f9adb2650ffd63ba845e6f9b806476fb147` |
| Package original | `com.unitvnet.tvod` | `com.unitvnet.mobs` |
| DEX classes declaradas | 7 | 11 |
| Application | `com.secneo.apkwrapper.AW` | `com.secneo.apkwrapper.AW` |
| Launcher Leanback | Presente | Ausente |

**Comprovado:** metadados e hashes estáticos. Não foi recuperado o código completo, nem testada execução sobre a conta original.

**Não comprovado:** eventos internos de reprodução, APIs públicas/estáveis, deep links, mecanismos de autorização e integração do player protegido.

**Política:** não publicar APKs originais, credenciais, chaves, endereços de servidores extraídos ou informações de usuários neste repositório público. Não remover DRM nem contornar autenticação. A assinatura privada do app original não está disponível.

Para repetir o inventário, usar `python tools/apk_baseline.py --tv <apk-tv> --mobile <apk-mobile>` localmente; o script processa os pacotes sem modificá-los.

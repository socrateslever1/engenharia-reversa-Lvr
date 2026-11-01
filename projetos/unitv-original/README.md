# UniTVnet — estudo de fidelidade dos APKs originais

Este diretório existe para **engenharia reversa do UniTVnet original**, não para criar um concorrente ou um reprodutor genérico.

- [Regras de foco](FOCO.md)
- [Evidências da interface extraídas dos APKs](RELATORIO_EVIDENCIAS.md)
- `tools/apk_baseline.py`: auditoria local reprodutível, sem alterar originais.
- `tools/tests/test_apk_baseline.py`: testes com APK sintético.

Execute com **cópias autorizadas dos APKs**, fora do Git:

```bash
python3 tools/apk_baseline.py --tv /caminho/UniTVnet_1.10.0.apk --mobile /caminho/UniTVnet_1.11.0.apk
python3 -m unittest discover -s tools/tests -v
```

**Nenhum APK refeito está pronto.** A fonte original está protegida por Secneo. As integrações de catálogo, login e player não foram recuperadas e testadas. Não publicar APKs, endereços do serviço, chaves nem credenciais.

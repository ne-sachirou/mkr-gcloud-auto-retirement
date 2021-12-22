[![Test](https://github.com/ne-sachirou/mkr-gcloud-auto-retirement/actions/workflows/test.yml/badge.svg)](https://github.com/ne-sachirou/mkr-gcloud-auto-retirement/actions/workflows/test.yml)

# mkr-gcloud-auto-retirement

mkr-gcloud-auto-retirement は、Mackerel の Google Cloud インテグレーションで Compute Engine を監視してゐる時に、auto scale 等で Compute Engine instance が無くなったならば、Mackerel からも退役させる道具です。

Mackerel の Google Cloud インテグレーションでは、Compute Engine instance が増えた時は自動で監視對象に追加されますが、減った時は、メトリックは取得されなくなりますが退役はされません。mkr-gcloud-auto-retirement は Compute Engine instance 一覧と Mackerel のホスト一覧を定期的に照合して、instance の無くなったホストを退役させます。

- [Google Cloud インテグレーション - Compute Engine - Mackerel ヘルプ](https://mackerel.io/ja/docs/entry/integrations/gcp/gce)

## 實行方法

### 手元から實行する

Clojure と mkr を入れてください。

- [CLI ツール mkr を使う - Mackerel ヘルプ](https://mackerel.io/ja/docs/entry/advanced/cli)

設定の要る環境變數は以下です。

- `GCLOUD_PROJECT_ID` : 監視する Compute Engine instance のある Google Cloud project の ID です。
- `MACKEREL_APIKEY` : ホスト一覧を取得し、要らないホストを退役する爲の Mackerel の API キーです。Read と Write の權限が要ります
- `MACKEREL_HOST_ID` : `mkr wrap` に指定するホスト ID です。mkr-gcloud-auto-retirement が正常に實行されるかチェック監視するものです。
- `MACKEREL_ROLE` : Google Cloud インテグレーションで登錄されたホストのサービスとロールです (例: `service1:role1`)。このロールにあるホストは全て、一つの Google Cloud インテグレーションの設定から登錄されたものでなければなりません。他の Google Cloud インテグレーションの設定や Google Cloud インテグレーションによるものではないホストが混ざってゐた場合、閒違へて退役させる可能性があります。

google-cloud-java が認證できるやうにしておいてください ( https://github.com/googleapis/google-cloud-java#authentication )。典型的には `GOOGLE_APPLICATION_CREDENTIALS` 環境變數を設定します。必要な權限は、Compute Engine の Compute インスタンス管理者です。

以下の command で實行できます。

```sh
make run-local
```

### Kubernetes の CronJob として實行する

`deployments/` directory に Kubernetes の manifest と、Google Cloud Build の設定があります。

GitHub repository へ Cloud Build への接續を install し、git push でトリガーするやうにします。mkr-gcloud-auto-retirement は Google Kubernetes Engine に deploy されます。

Cloud Build のトリガーに設定する変数は以下です。

- `_GCLOUD_SERVICE_ACCOUNT_KEY_JSON` : Google Cloud API を實行する爲のサービスアカウントキーです。必要な權限は、Compute Engine の Compute インスタンス管理者です。
- `_MACKEREL_APIKEY` : ホスト一覧を取得し、要らないホストを退役する爲の Mackerel の API キーです。Read と Write の權限が要ります
- `_MACKEREL_HOST_ID` : `mkr wrap` に指定するホスト ID です。mkr-gcloud-auto-retirement が正常に實行されるかチェック監視するものです。
- `_MACKEREL_ROLE` : Google Cloud インテグレーションで登錄されたホストのサービスとロールです (例: `service1:role1`)。このロールにあるホストは全て、一つの Google Cloud インテグレーションの設定から登錄されたものでなければなりません。他の Google Cloud インテグレーションの設定や Google Cloud インテグレーションによるものではないホストが混ざってゐた場合、閒違へて退役させる可能性があります。

一時間毎に mkr-gcloud-auto-retirement が實行されます。

## 現狀の制限

- Cloud Build の設定等に一部、個人用の設定が紛れ込んでゐます。
- 一つの Mackerel ロールのホストしか自動退役させられません。複數のロールから自動退役されたい場合は、複數 CronJob を起動してください。
- Compute Engine instance と Mackerel ホストそれぞれの name しか比較しません。よりよくは、ホストの meta 等を見るべきです。
- asia-northeast1 以外の Google Cloud region には對應してゐません。

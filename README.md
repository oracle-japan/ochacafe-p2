# [Helidon](https://helidon.io/)を使ったアプリケーションの性能の違いを確認するデモ

For [OCHaCafe Premium - #2 クラウド・アプリケーションのパフォーマンス]([Helidon](https://helidon.io/))

## Index
- [デモ・コンテンツの概要](#デモ・コンテンツの概要)
- [トレーシングのコーディングについて](#トレーシングのコーディングについて)

## デモ・コンテンツの概要

今回のデモで使用したアプリケーションは下記の8つのコンテナ・イメージで構成されます。

- モノリシック・パターン
  - helidon-monolith-eshop
- 機能分離パターン
  - helidon-api-eshop-mall
  - helidon-api-eshop-cart
  - helidon-api-eshop-order
- キャッシュ+非同期・パターン
  - helidon-async-eshop-mall
  - helidon-async-eshop-cart
  - helidon-async-eshop-order
  - helidon-async-relay

全プロジェクトの概要は下記の通りです。

```text
ohcacafe-p2-demo
├── 01-mvn_all.sh [全プロジェクトのビルド・スクリプト]
├── 02-image_all.sh [全プロジェクトのイメージビルド・スクリプト]
├── 03-push_all.sh [全イメージをRegistryにPushするスクリプト]
├── 04-apply_all.sh [全アプリケーションをK8s上にデプロイするスクリプト]
├── 05-load_redis.sh [Redis上の初期データセットアップ用スクリプト]
├── README.md
├── data [セットアップ用のDDLとJSONファイル]
│   ├── CART_ITEM_TABLE.sql
│   ├── CART_TABLE.sql
│   ├── INVENTORY_TABLE.sql
│   ├── ORDER_ITEM_TABLE.sql
│   ├── ORDER_TABLE.sql
│   ├── PRODUCT_TABLE.sql
│   └── product.json
├── helidon-api-eshop-cart [機能分離パターンのe-shop (Cart)]
│   ├── Dockerfile
│   ├── pom.xml
│   └── src
├── helidon-api-eshop-mall [機能分離パターンのe-shop (Mall)]
│   ├── Dockerfile
│   ├── pom.xml
│   └── src
├── helidon-api-eshop-order [機能分離パターンのe-shop (Order)]
│   ├── Dockerfile
│   ├── pom.xml
│   └── src
├── helidon-async-eshop-cart [キャッシュ+非同期・パターンのe-shop (Cart)]
│   ├── Dockerfile
│   ├── pom.xml
│   └── src
├── helidon-async-eshop-mall [キャッシュ+非同期・パターンのe-shop (Mall)]
│   ├── Dockerfile
│   ├── pom.xml
│   └── src
├── helidon-async-eshop-order [キャッシュ+非同期・パターンのe-shop (Order)]
│   ├── Dockerfile
│   ├── pom.xml
│   └── src
├── helidon-async-relay [KafkaトピックからREST APIにリレーするMessage Relay (キャッシュ+非同期パターン・で利用)]
│   ├── Dockerfile
│   ├── pom.xml
│   └── src
├── helidon-monolith-eshop [モノリシック・パターンのe-shop]
│   ├── Dockerfile
│   ├── pom.xml
│   └── src
├── helidon-sample-util [Tracing等の共通で利用するユーティリティ]
│   ├── Dockerfile
│   ├── pom.xml
│   └── src
├── image.list
└── yaml [K8s上にデプロイするためのYAML]
    ├── api-eshop.yaml [機能分離パターンのe-shopをデプロイするYAML]
    ├── async-eshop-redis.yaml [キャッシュ+非同期・パターンのe-shopで利用するRedisをデプロイするYAML]
    ├── async-eshop.yaml [キャッシュ+非同期・パターンのe-shopをデプロイするYAML]
    ├── jaeger-all-in-one.yaml [jaeger-all-in-oneをデプロイするYAML]
    ├── monolith-eshop.yaml [モノリシック・パターンのe-shopをデプロイするYAML]
    └── service-monitor.yaml [Service Monitorを利用する場合のYAML(今回のデモでは未使用)]

```

プロジェクト全体の一覧は、以下を展開してご覧ください。

<details><summary>全プロジェクトのツリー構造</summary><div>

```text
ohcacafe-p2-demo
├── 01-mvn_all.sh
├── 02-image_all.sh
├── 03-push_all.sh
├── 04-apply_all.sh
├── 05-load_redis.sh
├── README.md
├── data
│   ├── CART_ITEM_TABLE.sql
│   ├── CART_TABLE.sql
│   ├── INVENTORY_TABLE.sql
│   ├── ORDER_ITEM_TABLE.sql
│   ├── ORDER_TABLE.sql
│   ├── PRODUCT_TABLE.sql
│   └── product.json
├── helidon-api-eshop-cart
│   ├── Dockerfile
│   ├── helidon-sample-util.jar
│   ├── pom.xml
│   └── src
│       └── main
│           ├── java
│           │   └── com
│           │       └── oracle
│           │           └── demo
│           │               ├── Main.java
│           │               ├── MainApplication.java
│           │               └── cart
│           │                   ├── CartProvider.java
│           │                   ├── CartResource.java
│           │                   ├── OrderProvider.java
│           │                   ├── ProductProvider.java
│           │                   ├── client
│           │                   │   ├── DelegateOrderResource.java
│           │                   │   └── DelegateProductResource.java
│           │                   └── entity
│           │                       ├── CartEntity.java
│           │                       ├── CartItemEntity.java
│           │                       ├── InventoryEntity.java
│           │                       ├── OrderEntity.java
│           │                       ├── OrderItemEntity.java
│           │                       └── ProductEntity.java
│           └── resources
│               ├── META-INF
│               │   ├── beans.xml
│               │   ├── microprofile-config.properties
│               │   └── persistence.xml
│               └── jbossts-properties.xml
├── helidon-api-eshop-mall
│   ├── Dockerfile
│   ├── helidon-sample-util.jar
│   ├── pom.xml
│   └── src
│       └── main
│           ├── java
│           │   └── com
│           │       └── oracle
│           │           └── demo
│           │               ├── Main.java
│           │               ├── MainApplication.java
│           │               └── mall
│           │                   ├── ProductProvider.java
│           │                   ├── ProductResource.java
│           │                   ├── client
│           │                   │   └── DelegateProductResource.java
│           │                   └── entity
│           │                       ├── ProductEntity.java
│           │                       └── SearchResultEntity.java
│           └── resources
│               ├── META-INF
│               │   ├── beans.xml
│               │   └── microprofile-config.properties
│               └── jbossts-properties.xml
├── helidon-api-eshop-order
│   ├── Dockerfile
│   ├── helidon-sample-util.jar
│   ├── pom.xml
│   └── src
│       └── main
│           ├── java
│           │   └── com
│           │       └── oracle
│           │           └── demo
│           │               ├── Main.java
│           │               ├── MainApplication.java
│           │               ├── order
│           │               │   ├── OrderProvider.java
│           │               │   ├── OrderResource.java
│           │               │   └── entity
│           │               │       ├── OrderEntity.java
│           │               │       ├── OrderItemEntity.java
│           │               │       └── SearchResultEntity.java
│           │               └── product
│           │                   ├── ProductProvider.java
│           │                   ├── ProductResource.java
│           │                   └── entity
│           │                       ├── InventoryEntity.java
│           │                       ├── ProductEntity.java
│           │                       └── SearchResultEntity.java
│           └── resources
│               ├── META-INF
│               │   ├── beans.xml
│               │   ├── microprofile-config.properties
│               │   └── persistence.xml
│               └── jbossts-properties.xml
├── helidon-async-eshop-cart
│   ├── Dockerfile
│   ├── helidon-sample-util.jar
│   ├── pom.xml
│   ├── src
│   │   └── main
│   │       ├── java
│   │       │   └── com
│   │       │       └── oracle
│   │       │           └── demo
│   │       │               ├── Main.java
│   │       │               ├── MainApplication.java
│   │       │               └── cart
│   │       │                   ├── CartProvider.java
│   │       │                   ├── CartResource.java
│   │       │                   ├── OrderProvider.java
│   │       │                   ├── ProductProvider.java
│   │       │                   ├── ProductResource.java
│   │       │                   └── entity
│   │       │                       ├── CartEntity.java
│   │       │                       ├── CartItemEntity.java
│   │       │                       ├── OrderEntity.java
│   │       │                       ├── OrderItemEntity.java
│   │       │                       └── ProductEntity.java
│   │       └── resources
│   │           ├── META-INF
│   │           │   ├── beans.xml
│   │           │   └── microprofile-config.properties
│   │           └── jbossts-properties.xml
│   └── target
│       ├── maven-status
│       │   └── maven-compiler-plugin
│       │       └── testCompile
│       │           └── default-testCompile
│       │               ├── createdFiles.lst
│       │               └── inputFiles.lst
│       └── test-classes
│           └── META-INF
│               └── microprofile-config.properties
├── helidon-async-eshop-mall
│   ├── Dockerfile
│   ├── helidon-sample-util.jar
│   ├── pom.xml
│   └── src
│       └── main
│           ├── java
│           │   └── com
│           │       └── oracle
│           │           └── demo
│           │               ├── Main.java
│           │               ├── MainApplication.java
│           │               └── mall
│           │                   ├── ProductProvider.java
│           │                   ├── ProductResource.java
│           │                   └── entity
│           │                       ├── ProductEntity.java
│           │                       └── SearchResultEntity.java
│           └── resources
│               ├── META-INF
│               │   ├── beans.xml
│               │   └── microprofile-config.properties
│               └── jbossts-properties.xml
├── helidon-async-eshop-order
│   ├── Dockerfile
│   ├── helidon-sample-util.jar
│   ├── pom.xml
│   └── src
│       └── main
│           ├── java
│           │   └── com
│           │       └── oracle
│           │           └── demo
│           │               ├── Main.java
│           │               ├── MainApplication.java
│           │               ├── order
│           │               │   ├── OrderProvider.java
│           │               │   ├── OrderResource.java
│           │               │   └── entity
│           │               │       ├── OrderEntity.java
│           │               │       ├── OrderEntryEntity.java
│           │               │       ├── OrderItemEntity.java
│           │               │       └── SearchResultEntity.java
│           │               └── product
│           │                   ├── ProductProvider.java
│           │                   ├── ProductResource.java
│           │                   └── entity
│           │                       ├── InventoryEntity.java
│           │                       ├── ProductEntity.java
│           │                       └── SearchResultEntity.java
│           └── resources
│               ├── META-INF
│               │   ├── beans.xml
│               │   ├── microprofile-config.properties
│               │   └── persistence.xml
│               └── jbossts-properties.xml
├── helidon-async-relay
│   ├── Dockerfile
│   ├── helidon-sample-util.jar
│   ├── pom.xml
│   └── src
│       └── main
│           ├── java
│           │   └── com
│           │       └── oracle
│           │           └── demo
│           │               ├── Main.java
│           │               └── messaging
│           │                   ├── MessageRelay.java
│           │                   ├── MessageRelayBuilderImpl.java
│           │                   ├── MessageRelayException.java
│           │                   └── MessageRelayImpl.java
│           └── resources
│               ├── META-INF
│               │   └── beans.xml
│               └── application.yaml
├── helidon-monolith-eshop
│   ├── Dockerfile
│   ├── helidon-sample-util.jar
│   ├── pom.xml
│   └── src
│       └── main
│           ├── java
│           │   └── com
│           │       └── oracle
│           │           └── demo
│           │               ├── Main.java
│           │               ├── MainApplication.java
│           │               ├── cart
│           │               │   ├── CartProvider.java
│           │               │   ├── CartResource.java
│           │               │   └── entity
│           │               │       ├── CartEntity.java
│           │               │       └── CartItemEntity.java
│           │               ├── mall
│           │               │   ├── ProductProvider.java
│           │               │   ├── ProductResource.java
│           │               │   └── entity
│           │               │       ├── ProductEntity.java
│           │               │       └── SearchResultEntity.java
│           │               └── order
│           │                   ├── OrderProvider.java
│           │                   ├── OrderResource.java
│           │                   └── entity
│           │                       ├── OrderEntity.java
│           │                       ├── OrderItemEntity.java
│           │                       └── SearchResultEntity.java
│           └── resources
│               ├── META-INF
│               │   ├── beans.xml
│               │   ├── microprofile-config.properties
│               │   └── persistence.xml
│               └── jbossts-properties.xml
├── helidon-sample-util
│   ├── Dockerfile
│   ├── pom.xml
│   └── src
│       └── main
│           ├── java
│           │   └── com
│           │       └── oracle
│           │           └── demo
│           │               └── util
│           │                   ├── Converter.java
│           │                   ├── jaxrs
│           │                   │   └── CorsFilter.java
│           │                   ├── observable
│           │                   │   ├── AbstractTraceScope.java
│           │                   │   ├── CDITraceScope.java
│           │                   │   ├── TraceExtractor.java
│           │                   │   ├── TraceInjector.java
│           │                   │   ├── TraceScope.java
│           │                   │   ├── Traceable.java
│           │                   │   └── TraceableInterceptor.java
│           │                   └── streaming
│           │                       └── KafkaProvider.java
│           └── resources
│               └── META-INF
│                   └── beans.xml
├── image.list
└── yaml
    ├── api-eshop.yaml
    ├── async-eshop-redis.yaml
    ├── async-eshop.yaml
    ├── jaeger-all-in-one.yaml
    ├── monolith-eshop.yaml
    └── service-monitor.yaml
```

</div></details>

## 構成条件
事前にKubernetesクラスタ、及びKafkaクラスタ環境、及びOracle Database(またはOracle Database Express Edition)の環境を用意ください。

構成の条件は以下の通りです。
- Kubernetesクラスタ内のPodからOracle Databaseにアクセス可能なネットワーク構成であること。
- Kubrenetesクラスタ内のPodからKafkaクラスタにアクセス可能なネットワーク構成であること。
- ビルド環境から `kubectl` によるKubernetesクラスタへのアクセスが可能であること。
- Kubernetesクラスタからコンテナ・レジストリに対して直接アクセス可能であること。
- Oracle Databaseのリソース権限を与えられたユーザ `demouser` が作成されていること。

Oracle Cloud Infrastructureをご利用の場合は、下記の各サービスがご利用いただけます。
- Kubernetesクラスタ
  - [Oracle Container Engine for Kubernetes (OKE)](https://docs.cloud.oracle.com/ja-jp/iaas/Content/ContEng/Concepts/contengoverview.htm)
- コンテナ・レジストリ
  - [Oracle Cloud Infrastructure - Registry (OCIR)](https://docs.cloud.oracle.com/ja-jp/iaas/Content/Registry/Concepts/registryoverview.htm)
- Kafkaクラスタ
  - [Oracle Cloud Infrastructure - Streaming](https://docs.cloud.oracle.com/ja-jp/iaas/Content/Streaming/Concepts/streamingoverview.htm)
- Oracle Database
  - [Oracle Cloud Infrastructure - Database](https://docs.cloud.oracle.com/ja-jp/iaas/Content/Database/Concepts/overview.htm)

## ビルド・動作確認の手順

動作させてみたい方は、こちらを展開してご覧ください。

<details><summary>ビルド・動作確認方法の概要</summary><div>

### 1. 設定ファイルの事前設定
事前に以下のファイルを適切に修正してください。

#### 1-1. コンテナ・イメージのタグ名の設定

`03-push_all.sh` 内の `TAG_PREFIX` の各パラメータを設定します。

|パラメータ名|設定値|
|---|---|
|\<region-key\>|利用するクラウド環境リージョンのリージョン・キー|
|\<tenancy-namespace\>|Oracle Cloudのテナント名|
|\<repo-name\>|任意のリポジトリ名|

#### 1-2. KubernetesにPullするコンテナ・イメージ名の設定

各YAMLファイル (monolith-eshop.yaml / api-eshop.yaml / async-eshop.yaml) のDeployment内に定義される `image` の各パラメータを、上記1と同様の値を設定してください。

#### 1-3. コンテナ・イメージをPullする際のシークレットの設定

各YAMLファイル (monolith-eshop.yaml / api-eshop.yaml / async-eshop.yaml) のDeployment内に定義される `imagePullSecrets/name` の `<ocir secret>` の値に、[「Kubernetesのデプロイ中にレジストリからイメージをプルする」](Kubernetesのデプロイ中にレジストリからイメージをプルする)に記載された手順で作成するDockerシークレットを設定してください。

#### 1-4. 各アプリケーションが参照するデータベース接続情報の設定

各YAMLファイル (monolith-eshop.yaml / api-eshop.yaml / async-eshop.yaml) のDeployment内の環境変数として定義される、下記の環境変数のにデータベース接続設定の値を設定してください。

|環境変数名|パラメータ名|値|
|---|---|---|
|JAVAX_SQL_DATASOURCE_DEFAULTDATASOURCE_URL|\<db hostname\>|Podからアクセス可能なデータベースのホスト名|
||\<db service name\>|Oracle Database サービス名|
|JAVAX_SQL_DATASOURCE_DEFAULTDATASOURCE_PASSWORD|\<db passwd\>|demouserのパスワード|

#### 1-5. キャッシュ+非同期・パターンのKafka(Streaming)への接続設定

YAMLファイル async-eshop.yaml のDeployment内の環境変数として定義される、下記のStreaingへの接続設定値を設定してください。

|環境変数名|パラメータ名|値|
|---|---|---|
|KAFKA_CONNECTION_BOOTSTRAP_DASH_SERVERS|\<oci streaming endpoint\>|ストリーム・プール・アクセスのFQDN+9092番ポート<br/>例:cell-1.streaming.ap-osaka-1.oci.oraclecloud.com:9092|
|KAFKA_CONNECTION_SASL_DASH_JAAS_DASH_CONFIG|\<tenancy-namespace\>|Oracle Cloudのテナント名|
||\<idcs-username\>|Identity Cloudユーザ名|
||\<streampool-ocid\>|ストリーム・プールのOCID|

### 2. スクリプトの実行

以下の各スクリプトを順番に実行してください。

|実行スクリプト|実行内容|補足|
|---|---|---|
|01-mvn_all.sh|全プロジェクトのMavenビルド||
|02-image_all.sh|全プロジェクトのDockerイメージの作成|環境によって長時間(10分以上)かかります|
|03-push_all.sh|全プロジェクトのDockerイメージのPush|事前にdocker loginが必要です|
|04-apply_all.sh|全アプリケーションをKubernetes上にデプロイ(kubectl apply)||
|05-load_redis.sh|Redis上の初期データセットアップ用スクリプト||

なお、jaeger-all-in-one の Pod は上記のスクリプトではデプロイされません。必要に応じて手動で kubectl apply してください。

```shell-session
$ kubectl apply -f ./yaml/jaeger-all-in-one.yaml
```

### 3. DDLのインポート

./data 配下の \*.sql をインポートしてdemouserのスキーマに対してデータベース側の初期データをセットアップしてください。

### 4. 動作確認

`kubectl port-forward` で各アプリケーションのServiceに対してポートフォワードし、curl や Postman 等を利用してRESTエンドポイントにアクセスします。
例えば、monolith-eshopにアクセスする場合は以下のコマンドでポートフォワードした上で、`http://localhost:8080/` をルートとしたエンドポイントにアクセスしてください。

```shell-session
$ kubectl port-forward svc/monolith-eshop 8080:8080
```

機能ごとのエンドポイントのパスは以下の通りです。

|#|メソッド|パス|機能|
|---|---|---|---|
|1|GET|/product/search?offset=0&max=10|商品の一覧の取得 (0番目から10件)|
|2|GET|/product/PR000001000002|商品ID (PR000001000002) の商品情報の取得|
|3|POST|/cart/user1@oracle.com/PR000001000002|ユーザ (user1\@oracle.com) のカートに商品 (PR000001000002) を追加|
|4|GET|/cart/user1@oracle.com|ユーザ (user1\@oracle.com) のカート内容の取得|
|5|POST|/cart/checkout/user1@oracle.com|ユーザ (user1\@oracle.com) のカートの内容をチェックアウト|
|6|GET|/order/user1@oracle.com|ユーザ (user1\@oracle.com) の注文一覧の取得|

上記のエンドポイントと各Serviceの対応は以下の通りです。

||1|2|3|4|5|6|
|---|---|---|---|---|---|---|
|monolith-eshop|O|O|O|O|O|O|
|api-eshop-mall|O|O|||||
|api-eshop-cart|||O|O|O||
|api-eshop-order||||||O|
|async-eshop-mall|O|O|||||
|async-eshop-cart|||O|O|O||
|async-eshop-order||||||O|

</div></details>

## トレーシングのコーディングについて

### トレーシングのコードとビジネス・ロジックの分離

トレーシングは[Microservice chassis](https://microservices.io/patterns/microservice-chassis.html)にもある通り、ビジネス・ロジックに依存しないCross-cutting concernのため、その実装を直接アプリケーションコードに埋め込むと、ビジネス・ロジックのコードを汚してしまう恐れがあります。
さらに、システム全体のトレーサビリティを維持するために「どのような情報」を「どのような形式」で残すべきかはサービス間で共通化することが重要です。

まず、単純に OpenTracing API を使ってビジネス・ロジックのコードと一緒に直接トレース情報を埋め込んだ場合のコードを示します。

```java
public SearchResultEntity search(String offset, String limit, String sort) {
  SearchResultEntity result = null;
  Span span = null; // メソッド呼出し中のSpanを開始

  try {
    span = tracer.buildSpan("com.oracle.demo.mall.ProductProvider.search")
        .asChildOf(tracer.activeSpan())
        .withTag("span.kind", "app")
        .withTag("app.class", "com.oracle.demo.mall.ProductProvider")
        .withTag("app.method", "search")
        .withTag("app.arg_0", offset)
        .withTag("app.arg_1", limit)
        .withTag("app.arg_2", sort)
        .start();

    Span subSpan = tracer.buildSpan("search_products")
        .asChildOf(tracer.activeSpan())
        .withTag("span.kind", "app")
        .withTag("app.class", "com.oracle.demo.mall.ProductProvider")
        .withTag("app.method", "search")
        .withTag("app.arg_0", offset)
        .withTag("app.arg_1", limit)
        .withTag("app.arg_2", sort)
        .start();

    // このメソッド内のビジネス・ロジック
    result = getClient().search(offset, limit, sort);

    subSpan.finish();

    span.finish(); // メソッド呼出し中のSpanを終了

    return result;

  } catch (Exception ex2) {
    span.setTag("error", true);
    span.setTag("app.exception", ex2.getClass().getName());
    span.finish();
    throw ex;
  }
}
```

ご覧の通り大半のコードがトレーシングのためのコードになってしまい、ビジネス・ロジックが埋もれてしまいます。
同等のことが次のような形で実装できると、トレーシングのコーディングを省力化でき、ソースコードの見通しも非常にシンプルになります。

```java
@Traceable // 1. アノテーションのみでメソッド呼出しをトレース情報として残す
public SearchResultEntity search(String offset, String limit, String sort) {
  try {
    Scope scope = tracer.begin("search_products"); // 2. one linerでSpanの開始を宣言する

    // このメソッド内のビジネス・ロジック
    SearchResultEntity result = getClient().search(offset, limit, sort);

    tracer.end(scope); // 2. one linerでSpanの終了を宣言する

    return result;
  } catch (Exception ex) {
    ex.printStackTrace();
    throw ex;
  }
}
```

今回の方法は次の2つの手法でビジネス・ロジックのコードとトレーシングのコードを極力分離する実装をしています。

- [CDI Interceptorでトレーシングのコードを切り離す](#CDI-Interceptorでトレーシングのコードを切り離す)
- [one linerでbegin/endを記述できるようにする](#one-linerでbeginendを記述できるようにする)

### CDI Interceptorでトレーシングのコードを切り離す

CDI InterceptorはCDIでインジェクションされるオブジェクトのメソッドが、プロキシ・オブジェクトから呼び出される前後に特定の定型的な処理を実行させる仕組みです。メソッド呼出しの前後でログ出力を行う、今回のようなトレース情報を残す、などの共通的な処理をビジネス・ロジックから切り離す用途として利用されます。

```java
@Traceable // 1. アノテーションのみでメソッド呼出しをトレース情報として残す
public SearchResultEntity search(String offset, String limit, String sort) {
```

のように @Traceable アノテーションを付与したメソッドが呼び出される前後で、トレース情報を残す処理を共通処理としてInterceptorを実装します。
Interceptorの実装には以下の2つを作成します。

1. 適用するInterceptorを紐づけるためのアノテーション (上述の [\@Traceable](./helidon-sample-util/src/main/java/com/oracle/demo/util/observable/Traceable.java) に相当)

2. Interceptorの実装 (今回は[TraceableInterceptor](./helidon-sample-util/src/main/java/com/oracle/demo/util/observable/TraceableInterceptor.java))

※上記1.に関しては適用するターゲットの指定のみのため解説は割愛します。

トレースを記録するInterceptorの実装は以下のようになります。

```java
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
@Traceable
final class TraceableInterceptor {
	private TracerProducer producer;
	private Tracer tracer;

	@Inject
	public TraceableInterceptor(TracerProducer producer) {
		this.producer = producer;
		this.tracer = this.producer.tracer();
	}

	@AroundInvoke
	public Object obj(InvocationContext ic) throws Exception {
		Object result = null;
		String className = ic.getMethod().getDeclaringClass().getName();
		String methodName = ic.getMethod().getName();
		String operationName = className + "." + methodName; // operationName = <package>.<class>.<method>
```

このように @Interceptor のアノテーションを付与したクラスに、関連付けるアノテーション @Traceable を指定します。
@AroundInvoke のアノテーションを付与したメソッドが、インジェクションされるオブジェクトのメソッドを呼び出す際に呼び出され、引数のInvocationContextから呼出し対象となるメソッドの、クラス名/メソッド名/メソッド引数等を抽出することができます。

```java
		// 呼び出したクラス、メソッドの情報をタグに付与
		SpanBuilder builder = tracer.buildSpan(operationName)
				.asChildOf(tracer.activeSpan())
				.withTag("span.kind", "app")
				.withTag("app.class", className)
				.withTag("app.method", methodName);
		Span span = null;
		Scope scope = null;

		Object[] params = ic.getParameters();
		for (int i = 0; i < params.length; i++) { // メソッドのパラメータをタグに付与
			String key = "app.arg_" + Integer.toString(i);
			builder.withTag(key, params[i].toString());
		}

		try {
			span = builder.start();
			scope = tracer.scopeManager().activate(span);
			result = ic.proceed();
```

InvocationContext#proceed()を呼び出す事で、InterceptorはCDIでインジェクションされるオブジェクトのメソッドが実際に呼び出されますので、InvocationContext#proceed() の呼出し前でSpanを開始し、呼出し後にSpanを終了することでインジェクションされるオブジェクトのメソッドに対するトレース情報を残すことができます。

```java
			span.finish();
			scope.close();
		} catch (Exception ex) { // 例外発生時に例外情報をタグに付与
			span.setTag("error", true);
			span.setTag("app.exception", ex.getClass().getName());
			span.finish();
			scope.close();
			ex.printStackTrace();
			throw ex;
		}

		return result;
	}
}
```

また、インジェクションされるオブジェクトのメソッドが例外をスローした場合は、上記のように例外をキャッチしてエラーとしてトレースを残せます。


### one linerでbegin/endを記述できるようにする

コードブロックの特定の範囲を指定してトレースを残したい場合は、one linerで呼出した際に呼出し箇所(呼出し元のクラスやソースコード行番号など)に関する情報を取得する必要があります。今回はこれらの情報をスタックトレース情報から取得する方法で実現しています。(実装箇所は[AbstractTraceScope](./helidon-sample-util/src/main/java/com/oracle/demo/util/observable/AbstractTraceScope.java))

one linerで呼び出すメソッドに下記のようにStackTraceを取得するコードを記述します。

```java

@Override
public final Scope begin() {
  return begin(Thread.currentThread().getStackTrace()[2], null, null); // 呼出し元のコードのスタックトレース・エントリを元に生成
}
```

Thread#currentThread().getStackTrace()で返却される配列は以下の順でスタックトレース情報を保持します。

|getStackTraceのindex|保持するスタックトレース|
|---|---|
|Thread.currentThread().getStackTrace()[0]|getStackTrace()自身|
|Thread.currentThread().getStackTrace()[1]|getStackTrace()を呼び出している箇所、つまりAbstractTraceScope#begin()|
|Thread.currentThread().getStackTrace()[2]|AbstractTraceScope#begin()の呼び出した箇所|

呼出しメソッドから最終的に呼び出されるメソッドの実装は以下の通りです。

```java
private final Scope begin(StackTraceElement ste, String operationName, TraceExtractor extractor) {
  String className = ste.getClassName();
  String methodName = ste.getMethodName();
  int beginLineNo = ste.getLineNumber();

  operationName = (operationName == null ? String.format("%s.%s:%d", className, methodName, beginLineNo) : operationName);

  Tracer tracer = tracer();
  Span activeSpan = tracer.activeSpan();
  activeSpan = (activeSpan != null ? activeSpan : tracer.buildSpan(operationName).start());
  SpanContext parentContext = (extractor != null ? extractor.extract(tracer): activeSpan.context());

  SpanBuilder builder = tracer.buildSpan(operationName)
      .asChildOf(parentContext)
      .withTag("span.kind", "app")
      .withTag("app.class", className)
      .withTag("app.method", methodName)
      .withTag("app.begin_line", ste.getLineNumber());

  // Start span
  Span span = builder.start();

  return tracer.scopeManager().activate(span);
}
```

戻り値として返却される[Scope](https://www.javadoc.io/doc/io.opentracing/opentracing-api/0.32.0/io/opentracing/Scope.html)は、アクティブなSpanに関連付けられたSpanの終了を明示的に制御するオブジェクトです。今回は以下のように、このScopeオブジェクトだけを呼出し元のコードで保持し、Spanを終了させたい箇所で同様にone linerで記述できるように実装しています。

```java
  Scope scope = tracer.begin("search_products"); // 2. one linerでSpanの開始を宣言する

  // このメソッド内のビジネス・ロジック
  SearchResultEntity result = getClient().search(offset, limit, sort);

  tracer.end(scope); // 2. one linerでSpanの終了を宣言する
```

Spanを終了させる際の実装コードは以下のようになります。(前者が正常終了時、後者がエラー終了時に呼び出すメソッド)

```java
@Override
public final void end(Scope scope) {
  if (scope != null) {
    int endLineNo = Thread.currentThread().getStackTrace()[2].getLineNumber();
    Span span = this.tracer().activeSpan();
    span.setTag("app.end_line", endLineNo);
    span.finish();
    scope.close();
  }
}

@Override
public final void end(Scope scope, String errorMessage) {
  if (scope != null) {
    int endLineNo = Thread.currentThread().getStackTrace()[2].getLineNumber();
    Span span = this.tracer().activeSpan();
    span.setTag("app.end_line", endLineNo);
    if (errorMessage != null) {
      span.setTag("error", true);
      span.setTag("error_message", errorMessage);
      Map<String, String> map = new HashMap<String, String>();
      map.put("event", "error");
      span.log(map);
    }
    span.finish();
    scope.close();
  }
}
```

### 非同期連携時のトレーシングの伝播の実装

Helidonを始め、MicroProfileの実装ではRESTエンドポイントが呼び出された際に、フレームワーク内で自動でトレースを記録するよう実装されています。
今回のように、フレームワークに含まれない方式でのサービス間の連携では、明示的にトレース情報を伝播させる実装を行う必要があります。

今回の非同期連携ではKafkaを利用しているため、Kafkaレコードのヘッダをキャリアとして利用してKafkaを経由したサービス間でトレース情報を伝播します。

このようなケースでは、OpenTracing API の [Tracer](https://www.javadoc.io/static/io.opentracing/opentracing-api/0.32.0/io/opentracing/Tracer.html)の [inject メソッド](https://www.javadoc.io/static/io.opentracing/opentracing-api/0.32.0/io/opentracing/Tracer.html#inject-io.opentracing.SpanContext-io.opentracing.propagation.Format-C-)と [extract メソッド](https://www.javadoc.io/static/io.opentracing/opentracing-api/0.32.0/io/opentracing/Tracer.html#extract-io.opentracing.propagation.Format-C-)を利用します。

それぞれのメソッドの役割は以下の通りです。
- inject メソッド: 指定したSpanのコンテキストをキャリア・オブジェクトに注入する
- extract メソッド: キャリア・オブジェクトからSpanのコンテキスト情報を抽出する

Kafkaレコードのヘッダをキャリアとして利用するための injectメソッド/extractメソッドの利用例をそれぞれ紹介します。

**injectメソッドの利用例** (実装箇所:[TraceInjector](./helidon-sample-util/src/main/java/com/oracle/demo/util/observable/TraceInjector.java))

```java
@Override
public void inject(Tracer tracer) {
  Map<String, String> map = new HashMap<String, String>();
  TextMap textMap = new TextMapAdapter(map);

  tracer.inject(tracer.activeSpan().context(), Format.Builtin.HTTP_HEADERS, textMap);
  Headers kafkaHeaders = record.headers();

  for (String key : map.keySet()) {
    kafkaHeaders.add(key, map.get(key).getBytes());
  }
}
```
[TextMap](https://www.javadoc.io/static/io.opentracing/opentracing-api/0.32.0/io/opentracing/propagation/TextMap.html)をキャリアとしてTracer#injectメソッドでSpanContextを取得し、その内容をKafkaレコードのヘッダとして設定します。
このKafkaレコードをトピックにプロデュースすることでトレース情報を伝播できます。

**extractメソッドの利用例** (実装箇所:[TraceExtractor](./helidon-sample-util/src/main/java/com/oracle/demo/util/observable/TraceExtractor.java))
```
@Override
public SpanContext extract(Tracer tracer) {
  Map<String, String> map = new HashMap<String, String>();
  TextMap textMap = new TextMapAdapter(map);

  for (Header header : this.record.headers()) {
    map.put(header.key(), new String(header.value()));
  }

  return tracer.extract(Format.Builtin.HTTP_HEADERS, textMap);
}
```

Kafkaコンシューマ側では、コンシュームするKafkaレコードのヘッダをTextMapに詰め替え、Tracer#extractメソッドにキャリアとして渡してを呼び出すことでKafkaレコードのヘッダを経由して伝播したSpanContextを取得することができます。
この手順でSpanContextを取得すれば、下記のようなコードで伝播されたSpanContextを親としたSpanを開始することができます。

```java
Span span = tracer.buildSpan("operationName")
    .asChildOf(TraceExtractor.KafkaConsumerRecordCarrier.build(record))
    .start();
```

この手法は非同期連携に限らず、MicroProfileに含まれない方式での連携(JAX-RPC、JMS、データストアを経由した連携、etc.)が必要な際にも応用できますので、使いどころを抑えておきたいところです。

## 変更履歴

|Date|内容|
|---|---|
|2020.4.17|初版|

---
_Copyright © 2019-2020, Oracle and/or its affiliates. All rights reserved._

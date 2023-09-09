# Reactor

## Reactorとは

Reactorは完全にノンブロッキングなJVMにおけるリアクティブプログラミングの基盤を提供しており、[Reactive Streams](https://www.reactive-streams.org/)の仕様を実装している。

## Reactive Streamとは

> Reactive Streams は、ノンブロッキング バック プレッシャーによる非同期ストリーム処理の標準を提供する取り組みです
 
通常非同期メッセージング処理では、受信側のキャパシティを大きく超えるデータを送信し続けると、受信側がパンクしてオーバーフローしてしまう危険性がある。
しかし、そこで受信側から送信側に対してなんらかの方法で送信量を制御すると、システム間の結びつきを強めてしまい、疎結合でなくなってしまうという問題点がある。
そこで、Reactive Streamでは受信側が送信側に対して受け入れることが可能なデータ数を都度送信することで問題を解決する。この時受信側から送信側へのリクエストをバックプレッシャーと呼ぶ。

https://www.slideshare.net/ktoso/2014-akkastreamstokyojapanese

Reactive Streamに固有の用語については以下を参照
https://github.com/reactive-streams/reactive-streams-jvm/blob/v1.0.4/README.md#glossary

### Reactive Streamが定義しているAPIコンポーネント群

Reactive Streamでは以下のAPIが定義されている。

| API名         | 概要                                                                                                               | Reactorで対応するコンポーネント |
|--------------|------------------------------------------------------------------------------------------------------------------|---------------------|
| Publisher    | データストリームを提供する側<br>Subscriberからシグナルを受けとることでストリームを提供する                                                             | Mono,Fluxなど         |
| Subscriber   | データストリームを受け取る側<br>Publisherから通知されたシグナルに従い適切な処理を行う<br>Publisherに対してデータの提供は直接要求せず、Subscriptionに依頼する                | FlowSubscriberなど    |
| Subscription | データストリームとSubscriberの間の関係を定義する<br>バックプレッシャーの機能を提供し、Publisherに対してデータの提供を依頼する<br>このAPI単独では実行されず必ずSubscriber経由で実行される | MonoFlatMapなど       |
| Processor    | PublisherおよびSubscriberとして振舞う                                                                                     | FluxProcessorなど     |

#### Publisher

PublisherはSubscriptionからのシグナルに従い、Subscriberに対してデータストリームを提供する役目を負う。
Subscriber側へのデータの提供はSubscriberのinterfaceに定義されている`onNext`を呼び出す事で実施される。
この時シグナルは必ずSubscriptionから提供を依頼されたデータ件数以下とすることで、バックプレッシャーが実現することになる。

またデータストリームの状態がに従い、それぞれ特殊な対応するシグナルをSubscriberに通知する

| データストリームの状態         | Subscriber側メソッド |
|---------------------|-----------------|
| エラー                 | `onError`       |
| 有限のデータストリームを全て送り切った | `onComplete`    |

上記のイベントを通知した場合、Publisher側は*それ以上のデータをSubscriber側に提供しない*=(onNextを呼び出さない)責務を負い、データストリームが終了することになる。
ただし、ユーザー側で手動で呼び出してはならない。
逆にSubscriber側からデータストリームのキャンセルを要求することもできるが、この場合Subscriber -> Publisherの伝達の遅延などが考えられるため、要求後にデータが通知されないことは保証できない。

PublisherはSubscriberと1:1(unicast)、1:多(multicast)どちらも許容される。

#### Subscriber

SubscriberはSubscriptionを経由してPublisherに対してデータ提供の要求のシグナルを通知し、Publisherからのシグナルに応じて処理を実施する責務を負う。
Subscriberは以下の4つのメソッドで成り立ち、それぞれのシグナルが対応する。

| メソッド        | 概要                                                         |
|-------------|------------------------------------------------------------|
| onSubscribe | Publisherに対してデータストリームの提供開始を依頼する<br>実際の処理はSubscriptionが実施する |
| onNext      | Publisherから呼び出され、次のデータの処理を実施する                             |
| onError     | Publisherでエラーが生じた際に呼び出され、エラーハンドリングを実施する                    |
| onComplete  | 有限のデータストリームが全て流れ切った場合に呼び出される                               |


#### Subscription

SubscriptionはSubscriberとデータストリームの仲立ちをし、Publisherに対してデータストリームの送信を要求したり、データストリームをキャンセルしたりする責務を負う。
また、SubscriptionはSubscriberのコンテキスト内でのみ呼び出される。これによって、Publisher-Subscriberの間の関係が一意になることを強制する。

#### Processor

ProcessorはSubscriberとPublisher両方の責務を負う。

### 追記

#### Reactorにおける各種オブジェクトとスレッドの関係性について

> Reactor は、RxJava と同様、同時実行性に依存しないと考えることができます。つまり、同時実行モデルは強制されません。むしろ、開発者が主導権を握ることができます。ただし、だからといって、ライブラリによる同時実行の支援が妨げられるわけではありません。
https://projectreactor.io/docs/core/release/reference/#schedulers

基本的にはMonoやFluxを生成したスレッドで処理が実施され、それ以降のオペレータも同じスレッド上で実施される。

https://stackoverflow.com/questions/62138638/what-does-the-term-concurrency-agnostic-means-exactly
https://cero-t.hatenadiary.jp/entry/20171215/1513290305
https://fits.hatenablog.com/entry/2016/12/08/232622

ただし、subscribeOnやpublisherOnなどによってユーザーが任意に指定してマルチスレッドで実施することができる。
詳しくは後のチャプターで

## Chapter1

### ReactorにおけるReactive Stream APIの実装

#### Publisher

ReactorにおけるReactive StreamのPublisher interfaceの実装は大きく以下の2通りになる。

| 実装   | 概要                          | 参考                                                          |
|------|-----------------------------|-------------------------------------------------------------|
| Flux | 0～N個のデータを含むデータストリームを提供する    | https://projectreactor.io/docs/core/release/reference/#flux |
| Mono | 0 or 1個のデータを含むデータストリームを提供する | https://projectreactor.io/docs/core/release/reference/#mono |

#### Subscriber

ReactorにおけるReactive StreamのPublisher interfaceの実装として代表的なものを以下に示す

| 実装               | 概要                                              | 参考                                                                                               |
|------------------|-------------------------------------------------|--------------------------------------------------------------------------------------------------|
| BaseSubscriber   | ユーザーが独自実装のSubscriberを構築する際のベースとなるクラス            | https://projectreactor.io/docs/core/release/reference/#_an_alternative_to_lambdas_basesubscriber |
| LambdaSubscriber | Flux/Monoの提供しているsubscribeメソッド経由で実装できるSubscriber | -                                                                                                |

Reactorでは、BaseSubscriberをユーザーが独自に実装して利用することも出来るが、Flux/Monoの提供しているsubscribeメソッドに対して、
それぞれのイベント時の処理を実装した関数型インターフェースを引数として与えることで、間接的にLambdaSubscriberを実装し、onNextやonError,onCompleteなどの時の挙動を指定することもできる。

https://projectreactor.io/docs/core/release/reference/#_an_alternative_to_lambdas_basesubscriber

#### Subscription

ReactorではPublisher/Subscriberと異なり目立って実装されたクラスはない。そもそもユーザー側がrequestメソッドなどを呼び出すことを意識しないようにするという思想らしい。
Subscriberの時に説明したように、subscribeメソッドに対して任意の関数型インターフェースを引数に与えたり、BaseSubscriberの拡張時にhookOnSubscribeメソッドをoverrideすることで挙動を変更することができる。

#### Processor

割愛

### シンプルなReactorによるリアクティブプログラミングの実装

Reactor(Reactive Stream)を用いたリアクティブプログラミングでは、基本的にはPublisherがデータストリームを提供し、Subscriber側で各データを受け取り処理を実施するというのが基本的な構成である。
Publisherは提供するデータの性質に従って、Reactorより提供されているMono/Fluxいずれかを利用することができ、Subscriberは、ユーザー独自に実装することもできれば、Publisher側から提供されているメソッドを通じて実装することもできる。
また、基本的にPublisherはSubscriberされない限りデータストリームを流し始めないことに注意が必要。

## Chapter2

### Operator

Chapter1で説明したように、最もシンプルな形のReactorによる実装では、PublisherとSubscriberのみで完結するが、その処理には限度がある。
そこで、Reactorでは`operator`と呼ばれる種々のメソッドを提供することでデータストリームを様々な形で処理することが可能となっている。
operatorは基本的には前段のPublisherに対して処理を追加し、新しいPublisherでラップして後段にデータを流していく。
こうすることでPublisherは様々なoperatorによって順次処理が追加され、最終的にはSubscriberで処理されることになる。
この動作はJavaにおけるStream APIと同じような挙動となる。

※operator自体はReactive Streamには定義されていないことに注意

なお、operatorの実装的には独自にoperatorクラスが存在しているわけではなく、各Publisherに定義されていることに注意すること。
https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html

### Publisher/Operator/Subscriberの関係

Publisher/Operator/Subscriberは、従来の命令型プログラミングと比較すると相関がわかりにくい。
そのため、理解の一助として、JavaDocなどでPublisherの提供するデータストリームがoperator/subscriberなどがどのように処理されるかが図で表されている。

https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html

operatorは非常にたくさんの種類があることから、全て紹介することが不可能なので、代表的な2つを以下に示す。

#### Map

Streamのmapと同じでデータストリームから取得できた要素を1:1で変換するオペレータになる。
mapの特徴としては1:1で変換されることから、オペレータによる処理およびそれ以降の処理の順序性が担保されていることがあげられる。

https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#map-java.util.function.Function-

#### FlatMap

StreamのflatMapと同じでデータストリームから取得できた要素を1:Nで変換するオペレータになる。
flatMapの特徴としては、mapと異なりその実際はオペレータのなかでsubscribeを実施していること、
また1:Nで変換されることから、オペレータの処理自体は順序性が保証されているが、要素はフラットにされるため、それ以降の処理の順序性は保証されない。

https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#flatMap-java.util.function.Function-

順序性を保持したい場合はconcatMapやflatMapSequentialがある
https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#flatMapSequential-java.util.function.Function-
https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#concatMap-java.util.function.Function-

https://mike-neck.hatenadiary.com/entry/reactor-flux-flatten-3-patterns

## Chapter3

### エラーハンドリング

## Chapter4

### Test

## Chapter5

### バックプレッシャー


## Chapter6

### Thread/Scheduler

## Cahpter7

### Sink

## Chapter8

### デバッグ

## Cahpter 9

### メトリクス


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

#### Subscription

ReactorではPublisher/Subscriberと異なり目立って実装されたクラスはない。そもそもユーザー側がrequestメソッドなどを呼び出すことを意識しないようにするという思想らしい。
Subscriberの時に説明したように、subscribeメソッドに対して任意の関数型インターフェースを引数に与えたり、BaseSubscriberの拡張時にhookOnSubscribeメソッドをoverrideすることで挙動を変更することができる。

#### Processor

割愛

### シンプルなReactorによるリアクティブプログラミングの実装

Reactor(Reactive Stream)を用いたリアクティブプログラミングでは、基本的にはPublisherがデータストリームを提供し、Subscriber側で各データを受け取り処理を実施するというのが基本的な構成である。
Publisherは提供するデータの性質に従って、Reactorより提供されているMono/Fluxいずれかを利用することができ、Subscriberは、ユーザー独自に実装することもできれば、Publisher側から提供されているメソッドを通じて実装することもできる。
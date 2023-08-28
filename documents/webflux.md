\# WebFlux

## WebFluxとは

従来Spring FrameworkではWebフレームワークとしてサーブレットAPIおよびサーブレットコンテナ向けに開発されたSpring Web MVCが利用されてきた。
サーブレットAPIはその仕組み上同期かつブロッキングでしか処理ができないため、リクエストを受け付けられる上限がスレッド数に制限されたり、ブロッキング処理の為に非効率なリソースの使い方がされるケースがある。
そこで非同期、ノンブロッキングなストリーム処理を目指す仕様である`Reactive Stream`の実装を目指したウェブフレームワークがSpring WebFluxである。
Reactive Streamの実装として、WebFluxのリアクティブライブラリはReactorが利用されている。

## Reactive Streamとは

https://www.reactive-streams.org/
> Reactive Streams は、ノンブロッキング バック プレッシャーによる非同期ストリーム処理の標準を提供する取り組みです

通常非同期メッセージング処理では、受信側のキャパシティを大きく超えるデータを送信し続けると、受信側がパンクしてオーバーフローしてしまう危険性がある。
しかし、そこで受信側から送信側に対してなんらかの方法で送信量を制御すると、システム間の結びつきを強めてしまい、疎結合でなくなってしまうという問題点がある。
そこで、Reactive Streamでは受信側が送信側に対して受け入れることが可能なデータ数を都度送信することで問題を解決する。この時受信側から送信側へのリクエストをバックプレッシャーと呼ぶ。  

https://www.slideshare.net/ktoso/2014-akkastreamstokyojapanese

## Reactorとは

ReactorはSpring WebFluxのコア層となるReactive Streamを実装したリアクティブライブラリになる。
ReactorではReactive Streamの思想に則って、イベントの発行者であるPublisher、イベントの受信者であるSubscriber、その間で流れるメッセージをストリームと表現している。
Subscriber側では複数のコールバックメソッドが実装されており、ストリームからメッセージを取得したり、エラーが発生した場合に適切なコールバックメソッドを呼び出せるようになっている。

またReactorでは各種のoperatorを用いてPublisher側が送信するデータストリームに対して処理を加えたり、ラップすることができる。
operatorは複数種類繋げることができるが、最終的にSubscriber側がsubscribeしない限りメッセージは送出されずに処理されることはない。
subscribeされることによりチェーン全体のデータフローをトリガーすることができる。当然この時バックプレッシャーとして取得可能なメッセージ量をPublisher側に伝達することもできる。

https://projectreactor.io/

リアクターの用語

| 用語         | 意味                              |
|------------|---------------------------------|
| Publisher  | メッセージ送信側                        |
| Subscriber | メッセージ受信側                        |
| Stream     | Publisher/Subscriberの間で流れるメッセージ |

### Publisher

前述したようにPublisherはReactorにおけるストリームイベントの発行者になる。Reactorにおいて、Publisherの実装の代表的なものとしてMonoとFluxの2つがある。
Monoは空もしくは1つの値を取り扱い、Fluxは0個以上の値を取り扱う。後述するoperator演算子でそれぞれのデータストリームを操作することが出来るが、その操作の結果でFluxからMonoへ、またMonoからFluxへと変換が生じることもある。
いずれも`Publisher<T>`interfaceの実装になる。
operatorによる変換処理の結果はSubscriberのonNext,onComplete,onErrorメソッドへの呼び出しに変換される。

#### Publisherの作成

Mono/Fluxの生成メソッドは複数用意されているため以下参考のこと
https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html
https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html

#### subscribe

前述したように、Publisherが発行するストリームイベントはsubscribeしないとイベントが流れてこない。
subscribeする代表的な方法を以下に示す。また、operatorに関してはあくまで`ストリームイベントをfilterしたりする`のが主でsubscribeしたメッセージに対して何かやるものではない。

```java

public class SampleReactor {
    public static void main(String args[]){
        Mono.just(1).map( num -> "Mono : " + num).subscribe(System.out::println);
    }
}
```

上記コードでは
Publisherの生成 -> `Mono.just(1)`
Operator -> `.map( num -> "Mono : " + num)`
subscribe -> `.subscribe(System.out::println)`

上記例ではoperatorでintからstringに変換したのちconsoleに表示するというsubscriberを登録している。
subscriberは上記意外にもエラー時や、全ストリームイベントを消化した場合のsubscriberを登録することもできる。以下に例を示す。

```java
public class SampleReactor {

    public static void main(String args[]){

        Mono.just(1)
                .map( num -> "Mono : " + num)
                .subscribe(
                        System.out::println, //①
                        error -> System.out.println("Error: " + error), //②
                        () -> System.out.println("Complete") //③
                        );
    }
}
```

| 項番  | 説明                                      |
|-----|-----------------------------------------|
| ①   | ストリームイベント一つに対してサブスクライブした際に実行するconsumer  |
| ②   | ストリームイベントでエラーが発生した場合のconsumer           |
| ③   | ストリームイベントのシーケンス全体が正常終了した場合に実行するconsumer |

```java
public class SampleReactor {

    public static void main(String args[]){

        Flux.range(1,4)
            .map( num -> {
                if(num<=3) return "Flux : " + num;
                throw new RuntimeException();
            })
            .subscribe(
                System.out::println,
                error -> System.out.println("Error: " + error),
                () -> System.out.println("Complete")
            );
    }
}
```
エラーが発生した場合にコンソールにエラー内容に表示する

### Operator

前述のコードのように、FluxやMonoで表現されるPublisherは、Subscriberによって最終的な処理が行われるが、publisherとsubscriberの間でデータストリームを処理するものをoperatorと呼称する。
FluxやMonoおよびSubscriberとoperatorの関係図は以下を参照のこと
https://projectreactor.io/docs/core/release/reference/#flux
https://projectreactor.io/docs/core/release/reference/#mono

operatorの種類は以下のjavadocからもわかるように種類が非常に大いが、代表的なものを説明する
https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html
https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html

#### Map/flatMap

operatorの一番基本的なものがmapおよびflatMapである。
どちらも1つのデータストリームに対して設定された処理を行い、後ろに渡すが異なる点が存在する

1. mapは1:1 flatmapは1:多の変換を行う
2. mapはデータストリームの入力順に処理されるが、flatMapはデータストリームの各要素に対して非同期で処理される
```java
public class SampleReactor {

    public static void main(String args[]){

        Flux.range(1,4)
            .flatMap( num -> Flux.just(num, num *2))
                .subscribe(System.out::println,
                error -> System.out.println("ERROR " + error.getMessage()));
    }
}
```

上記の例では、1から4のデータストリームがflatMapで処理されnum, num *2のfluxが出力され、subscribeされる

### エラー処理

Reactorにおけるエラーはその時点でストリームイベントを終了する挙動となり、Subscriberに定義されたメソッドとそのonErrorメソッドに伝搬していく。
onErrorの処理を行う場合、エラーを発生させたストリームイベントは終了し、onErrorでまた新しくストリームイベントが開始されるようになる。
同期、ブロッキング処理におけるエラーハンドリングとの対比を含め、以下にReactorにおける種々のハンドリング方式を乗せる。

```java
public class SampleReactor {

    public static void main(String args[]){

        Flux.range(1,4)
            .map( num -> {
                if(num<=3) return "Flux : " + num;
                throw new RuntimeException();
            })
                .map(str -> "Map 2 "+ str)
            .subscribe(
                System.out::println,
                error -> System.out.println("Error: " + error)
            );
    }
}
```

上記コードはsubscribeメソッドにおけるエラーハンドリング可能なconsumerでハンドリングされる。
通常のJavaコードのtry-catcheと同じようにoperator演算子で例外が発生した場合、それ以降の処理を打ち切ってハンドリングを実施する。

```java
public class SampleReactor {

    public static void main(String args[]){

        Flux.range(1,4)
            .map( num -> {
                if(num<=2) return "Flux : " + num;
                throw new RuntimeException();
            })
                .map(str -> "Map 2 "+ str)
            .onErrorReturn("Return")
                .subscribe(System.out::println,
                error -> System.out.println("ERROR " + error.getMessage()));
    }
}
```

上記のコードでは、先ほどのsubscribeにようエラーハンドリングと異なり、onErrorReturnでハンドリングを行う。
こちらもエラー発生時点でそれ以降のデータストリームの処理を終了するが、異なる点としてonErrorReturnで代替となるデータストリームを発信することがあげられる。
今回の場合は固定で`Return`という文字列を返すようになる。そのためsubscribeに登録されたエラーハンドリングは実施されない。

```
.onErrorReturn(e-> e.getMessage().equals("Flux : 3"),"Return2")
```
上記のようにエラーメッセージによってハンドリングするかしないかを決定することができる。

```java
public class SampleReactor {

    public static void main(String args[]){

        Flux.range(1,4)
            .map( num -> {
                if(num<=2) return "Flux : " + num;
                throw new RuntimeException();
            })
                .map(str -> "Map 2 "+ str)
            .onErrorComplete()
                .subscribe(System.out::println,
                error -> System.out.println("ERROR " + error.getMessage()));
    }
}
```
上記のようにonErrorCompleteを利用した場合は非常にシンプルで、エラー発生時にエラーを握りつぶしてデータストリームを正常終了させることができる。
onErrorCompleteではonErrorReturn同様にハンドリングする例外クラスを指定することもできる。

通常、エラーが発生した場合データストリームが終了するが、onErrorContinueにおいて、Publisherでラップしてやることでデータストリームを終了せずに実行することもできる。
```java
public class SampleReactor {

    public static void main(String[] args) {
        Flux.range(1, 10)
                .flatMap(SampleReactor::processNumber)
                .onErrorContinue((error, value) ->{
                    System.out.println("test");
                })
                .subscribe(System.out::println,
                        error -> System.out.println("ERROR " + error.getMessage()));
    }

    public static Mono<Integer> processNumber(int num) {
        if (num == 5) {
            return Mono.error(new RuntimeException("Error occurred for number 5."));
        }
        return Mono.just(num);
    }
}
```

```java
public class SampleReactor {

    public static void main(String[] args) {
        Flux.range(1, 10)
                .flatMap(value -> processNumber(value)
                        .onErrorResume(e -> Flux.just(value * 2)))
                .subscribe(System.out::println,
                        error -> System.out.println("ERROR " + error.getMessage()));
    }

    public static Flux<Integer> processNumber(int num) {
        if (num == 5) {
            return Flux.error(new RuntimeException("Error occurred for number 5."));
        }
        return Flux.just(num);
    }
}
```

processNumberであらたなデータストリームが生成されているので、上記の場合は大元のデータストリームが中止されることはない。

```java
public class SampleReactor {

    public static void main(String[] args) {
        Flux.range(1, 10)
                .flatMap(SampleReactor::processNumber)
                .onErrorMap(e -> new RuntimeException("Error Fall back"))
                .subscribe(System.out::println,
                        error -> System.out.println("ERROR " + error.getMessage()));
    }

    public static Flux<Integer> processNumber(int num) {
        if (num == 5) {
            return Flux.error(new RuntimeException("Error occurred for number 5."));
        }
        return Flux.just(num);
    }
}
```
また、上記のようにエラーを再度Wrapすることもできる。
その他に、通常のストリームには影響せずにログなどに出力したい場合は以下のようにする。

```java
public class SampleReactor {

    public static void main(String[] args) {
        Flux.range(1, 10)
                .flatMap(value -> SampleReactor.processNumber(value).doOnError(e -> System.out.println(e.getMessage()))) //1
                .doOnError(e -> System.out.println(e.getMessage())) // 2
                .subscribe(System.out::println,
                        error -> System.out.println("ERROR " + error.getMessage()));
    }

    public static Flux<Integer> processNumber(int num) {
        if (num == 5) {
            return Flux.error(new RuntimeException("Error occurred for number 5."));
        }
        return Flux.just(num);
    }
}
```

1もしくは2の位置にてdoOnErrorを用いることによって、ストリームに影響せずに種々の処理を行える。
`doOn`がつくメソッドでは他も同様に、主流のデータストリームに影響を及ぼさずに、ストリームの中身のデータを除き、副作用を生じさせることもできる。

### Sinks

SinksはReacor Coreで提供されるスレッドセーフにリアクティブストリームのシグナルを手動でトリガーできるようにするクラスになる。
Sinksを利用することにより、アプリで生成や収集したデータを非同期かつストリームの状態で後段のシステムに連携することができる。
例えば、APIからデータを受け取ったアプリが後段のMessaging Queueに非同期かつストリームの状態でデータを流したり、外部のイベントを非同期に実行することもできるようになる。


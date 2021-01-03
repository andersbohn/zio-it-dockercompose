import zio.{Has, Layer, Task, UIO, URIO, ZLayer}

object ServiceModule {

  type Aa = Has[Aa.Service]

  object Aa {
    trait Service {
      def helloA(i:Int): UIO[String]
    }

    object Service {
      val fake: Service = new Service {
        override def helloA(i: Int): UIO[String] = Task.succeed{
          println(s"helloA $i")
          s"helloA$i"
        }
      }
    }

    val fake: Layer[Nothing, Aa] =
      ZLayer.succeed(Service.fake)

  }

}

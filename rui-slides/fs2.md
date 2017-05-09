## FS2

- Stream[F, O]
- Pipe[F, I, O] =:= Stream[F, I] => Stream[F, O]
- Sink[F, O] =:= Pipe[F, O, Unit]

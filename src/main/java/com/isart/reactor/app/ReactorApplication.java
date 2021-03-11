package com.isart.reactor.app;

import com.isart.reactor.app.models.Comentario;
import com.isart.reactor.app.models.Usuario;
import com.isart.reactor.app.models.UsuarioComentario;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class ReactorApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ReactorApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ReactorApplication.class, args);
    }

    @Override
    public void run(String... args) throws InterruptedException {
        ejemploIterable();
        ejemploFlatMap();
        ejemploFlatMapUsuarioToList();
        ejemploCollectList();
        ejemploUsuarioComentarioFlatMap();
        ejemploUsuarioComentarioZipWith();
        ejemploUsuarioComentarioZipWith2();
        ejemploZipWithRangos();
        ejemploInteval();
        ejemploDelayElements();
        ejemploIntervaloInfinito();
        ejemploIntervalCreate(false);
        ejemploContraPresion();
        ejemploContraPresionLimitRetate();
    }

    private void ejemploIterable() {
        List<String> usuariosList = new ArrayList<>();
        usuariosList.add("andres galarga");
        usuariosList.add("pablo carrazcal");
        usuariosList.add("linux tolvars");
        usuariosList.add("steved jobs");
        usuariosList.add("steved marck");
        Flux<String> nombres = Flux.fromIterable(usuariosList);
        Flux<Usuario> usuarioFlux = nombres.map(name -> new Usuario(name.split(" ")[0].toUpperCase().trim(),
                name.split(" ")[1].toUpperCase().trim()))
                .filter(usuario -> usuario.getNombres().equalsIgnoreCase("steved"))
                .doOnNext(x -> {
                    if (x == null) {
                        throw new RuntimeException("los valores no pueden ser nulos");
                    } else {
                        System.out.println(x.getNombres());
                    }
                }).map(x -> {
                    x.setNombres(x.getNombres().toLowerCase(Locale.ROOT));
                    return x;
                });

        usuarioFlux.subscribe(usuario -> log.info(usuario.toString()), error -> log.error(error.getMessage()),
                () -> System.out.println("el proceso ha finalizado")
        );
    }

    private void ejemploFlatMap() {
        List<String> usuariosList = new ArrayList<>();
        usuariosList.add("andres galarga");
        usuariosList.add("pablo carrazcal");
        usuariosList.add("linux tolvars");
        usuariosList.add("steved jobs");
        usuariosList.add("steved marck");
        Flux.fromIterable(usuariosList)
                .map(name -> new Usuario(name.split(" ")[0].toUpperCase(Locale.ROOT).trim(), name.split(" ")[1].toUpperCase(Locale.ROOT).trim()))
                .flatMap(usuario -> {
                    if(usuario.getNombres().equalsIgnoreCase("steved")) {
                        return Mono.just(usuario);
                    } else {
                        return Mono.empty();
                    }
                })
                .map(usuario -> {
                    usuario.setNombres(usuario.getNombres().toLowerCase(Locale.ROOT));
                    return usuario;
                })
                .subscribe(usuario -> log.info(usuario.toString()));
    }
    private List<Usuario> getUsuarioList() {
        List<Usuario> usuarioList = new ArrayList<>();
        usuarioList.add(new Usuario("andres", "galarga"));
        usuarioList.add(new Usuario("pablo", "carrazcal"));
        usuarioList.add(new Usuario("linux", "tolvars"));
        usuarioList.add(new Usuario("steved", "jobs"));
        usuarioList.add(new Usuario("steved", "marck"));
        return usuarioList;
    }

    private void ejemploFlatMapUsuarioToList() {
        List<Usuario> usuarioList = getUsuarioList();
        Flux.fromIterable(usuarioList)
                .map(usuario -> usuario.getNombres().toUpperCase(Locale.ROOT).concat(" ").concat(usuario.getApellidos().toUpperCase(Locale.ROOT)))
                .flatMap(nombre -> {
                    if(nombre.contains("steved".toUpperCase(Locale.ROOT))) {
                        return Mono.just(nombre);
                    } else {
                        return Mono.empty();
                    }
                })
                .map(nombre -> nombre.toLowerCase(Locale.ROOT))
                .subscribe(log::info);

    }

    private void ejemploCollectList() {
        List<Usuario> usuarioList = getUsuarioList();
        Flux.fromIterable(usuarioList)
                .collectList()
                .subscribe(list -> list.forEach(usuario -> System.out.println(usuario.toString())));
    }
    private Usuario getUsuario() {
        return new Usuario("Jhon", "Doe");
    }

    private Comentario getComentarios() {
        Comentario comentario = new Comentario();
        comentario.addComentario("hola, que qtel todo");
        comentario.addComentario("la casa de blanca");
        comentario.addComentario("hola, ya es la hora del tea");
        return comentario;
    }

    private Mono<Usuario> getUsuarioMono() {
        return Mono.fromCallable(this::getUsuario);
    }

    private Mono<Comentario> getComentarioMono(){
       return  Mono.fromCallable(this::getComentarios);
    }

    private void ejemploUsuarioComentarioFlatMap() {
        Mono<Usuario> usuarioMono = getUsuarioMono();
        Mono<Comentario> comentarioMono = getComentarioMono();
        usuarioMono.flatMap(usuario -> comentarioMono.map(comentario -> new UsuarioComentario(usuario, comentario)))
        .subscribe(usuarioComentario -> log.info(usuarioComentario.toString()));
    }


    private void ejemploUsuarioComentarioZipWith() {
        Mono<Usuario> usuarioMono = getUsuarioMono();
        Mono<Comentario> comentarioMono = getComentarioMono();
        Mono<UsuarioComentario> usuarioComentarioMono = usuarioMono.zipWith(comentarioMono, UsuarioComentario::new);
        usuarioComentarioMono.subscribe(usuarioComentario -> log.info(usuarioComentario.toString()));
    }

    private void ejemploUsuarioComentarioZipWith2() {
        Mono<Usuario> usuarioMono = getUsuarioMono();
        Mono<Comentario> comentarioMono = getComentarioMono();
        Mono<UsuarioComentario> usuarioComentarioMono = usuarioMono.zipWith(comentarioMono)
                .map(tuple -> new UsuarioComentario(tuple.getT1(), tuple.getT2()));
        usuarioComentarioMono.subscribe(usuarioComentario -> log.info(usuarioComentario.toString()));
    }

    private void ejemploZipWithRangos() {
        Flux.just(1, 2, 3, 4)
                .map(i -> (i * 2))
                .zipWith(Flux.range(0, 4), (uno, dos) -> String.format("primer parametro %s segundo parametro %d", uno, dos))
                .subscribe(log::info);
    }

    private void ejemploInteval() {
        Flux<Integer> rango = Flux.range(1, 12);
        Flux<Long> restraso = Flux.interval(Duration.ofSeconds(1));
        rango.zipWith(restraso, (rang, delay) -> rang)
                .doOnNext(i -> log.info(i.toString()))
                .subscribe();
    }

    private void ejemploDelayElements(){
        Flux<Integer> rango = Flux.range(1, 12)
                .delayElements(Duration.ofSeconds(1))
                .doOnNext(i -> log.info(i.toString()));
        rango.subscribe();
    }

    private void ejemploIntervaloInfinito() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Flux.interval(Duration.ofSeconds(1))
                .doAfterTerminate(latch::countDown)
                .flatMap(i -> i >= 5 ?
                        Flux.error(new InterruptedException("solo hasta 5!")) :
                        Flux.just(i))
                .map(i -> "elemento:  "+ i)
                .retry(2)
                .subscribe(log::info, e -> log.error(e.getMessage()));
        latch.await();
    }


    private void ejemploIntervalCreate(Boolean interrumpir) {
        Flux.create(emiter -> {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                private Integer c = 0;
                @Override
                public void run() {
                    emiter.next(++c);
                    if(c == 10) {
                        timer.cancel();
                        emiter.complete();
                    }
                    // codigo para probar interrupcion
                    if(interrumpir && c ==5) {
                        timer.cancel();
                        emiter.error(new InterruptedException("Error, se ha detenido el flujo"));
                    }
                }
            }, 100, 100);
        })
        .subscribe(next -> log.info(next.toString()),
                error -> log.error(error.getMessage()),
                () -> log.info("Flujo terminado"));
    }

    private void ejemploContraPresion() {
        Flux.range(1, 10)
                .map(Object::toString)
                .log()
                .subscribe(new Subscriber<>() {
                    private Subscription s;
                    private final Integer limit = 5;
                    private Integer consumer = 0;
                    @Override
                    public void onSubscribe(Subscription s) {
                        this.s = s;
                        s.request(limit);
                    }
                    @Override
                    public void onNext(String elemento) {
                        log.info(elemento);
                        consumer++;
                        if(consumer.equals(limit)) {
                            consumer = 0;
                            s.request(limit);
                        }
                    }
                    @Override
                    public void onError(Throwable t) {

                    }
                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void ejemploContraPresionLimitRetate() {
        Flux.range(1, 10)
                .map(Object::toString)
                .log()
                .limitRate(5)
                .subscribe();
    }




}

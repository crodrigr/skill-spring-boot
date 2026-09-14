# 💡 Ejemplo 10 — IoC Container: ApplicationContext y ciclo de vida de un bean

## 🏥 Caso de estudio

MediSalud: un `ServicioCitas` administrado por el contenedor IoC, para observar en
orden las fases de su ciclo de vida.

## 💻 Código

```java
@Component
public class ServicioCitas {

    public ServicioCitas() {
        System.out.println("1) Instanciación: el contenedor crea el objeto ServicioCitas");
    }

    @PostConstruct
    public void inicializar() {
        System.out.println("2) Inicialización: @PostConstruct — el bean ya tiene sus dependencias listas");
    }

    public void agendar(String paciente) {
        System.out.println("3) Uso: agendando cita para " + paciente);
    }

    @PreDestroy
    public void liberar() {
        System.out.println("4) Destrucción: @PreDestroy — el contenedor libera el bean antes de apagarse");
    }
}
```

```java
public class DemoContenedorIoC {
    public static void main(String[] args) {
        ConfigurableApplicationContext contexto =
                new AnnotationConfigApplicationContext(ConfiguracionApp.class);

        ServicioCitas servicioCitas = contexto.getBean(ServicioCitas.class);
        servicioCitas.agendar("Ana Gómez");

        contexto.close(); // dispara la fase de destrucción de los beans
    }
}
```

## 🧭 Explicación paso a paso

1. El `ApplicationContext` es el **contenedor IoC**: al arrancar, escanea las
   clases anotadas (`@Component`, entre otras) y decide qué beans crear.
2. **Instanciación**: el contenedor llama al constructor de `ServicioCitas`. El
   código cliente nunca escribe `new ServicioCitas()`.
3. **Inyección de dependencias**: si `ServicioCitas` dependiera de otro bean (por
   ejemplo, un repositorio), el contenedor se lo entregaría en este punto, antes de
   inicializar (no se muestra aquí porque este bean no tiene dependencias).
4. **Inicialización**: el método anotado `@PostConstruct` se ejecuta una sola vez,
   cuando el bean ya está completamente construido e inyectado; es el lugar
   correcto para lógica de arranque (por ejemplo, precargar una caché).
5. **Uso**: mientras la aplicación corre, el bean permanece disponible; el
   contenedor entrega la **misma instancia** cada vez que se pide (por defecto,
   los beans son *singleton*).
6. **Destrucción**: al cerrar el contexto (`contexto.close()`), el contenedor
   invoca `@PreDestroy` en cada bean, dando la oportunidad de liberar recursos
   (conexiones, hilos, archivos abiertos).

## ✅ Resultado esperado

```text
1) Instanciación: el contenedor crea el objeto ServicioCitas
2) Inicialización: @PostConstruct — el bean ya tiene sus dependencias listas
3) Uso: agendando cita para Ana Gómez
4) Destrucción: @PreDestroy — el contenedor libera el bean antes de apagarse
```

## 📌 Idea clave

El estudiante nunca instancia `ServicioCitas` con `new`: **pide** una instancia al
`ApplicationContext` (`contexto.getBean(...)`), o —más habitual en una aplicación
real— simplemente declara que otro bean lo necesita como dependencia, y el
contenedor resuelve toda esta secuencia por detrás.

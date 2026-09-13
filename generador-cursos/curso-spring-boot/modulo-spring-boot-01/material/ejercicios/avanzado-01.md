# 🔴 Avanzado 01 — Combinar polimorfismo con Inyección de Dependencias

## 🧩 Problema

Se necesita un `ServicioRecordatorios` que envíe recordatorios sin conocer el
medio concreto: en MediSalud se envían por SMS y en Biblioteca Universitaria por
email, y ambos casos deben poder resolverse con la **misma** clase de servicio.

## 💻 Código o contexto de partida

```java
public interface Notificador {
    void enviar(String destinatario, String mensaje);
}

// Faltan por implementar: NotificadorSms (MediSalud) y NotificadorEmail (Biblioteca)

public class ServicioRecordatorios {
    // completar: debe depender de Notificador, no de una implementación concreta
}
```

1. Implementá `NotificadorSms` y `NotificadorEmail`, cada uno con su propia forma
   de "enviar" (podés simularlo con un `System.out.println` descriptivo).
2. Completá `ServicioRecordatorios` para que reciba un `Notificador` por
   constructor y lo use en un método `enviarRecordatorio(String destinatario,
   String mensaje)`.
3. Escribí el código que crea **dos** instancias de `ServicioRecordatorios`: una
   para MediSalud (con `NotificadorSms`) y otra para Biblioteca Universitaria (con
   `NotificadorEmail`), sin modificar la clase `ServicioRecordatorios` entre un
   caso y otro.

## 📏 Criterios de evaluación de la solución

- `ServicioRecordatorios` depende únicamente de la interfaz `Notificador`, nunca
  de `NotificadorSms` ni `NotificadorEmail` directamente.
- `NotificadorSms` y `NotificadorEmail` implementan `Notificador` con
  comportamientos distintos y reconocibles.
- El mismo `ServicioRecordatorios` funciona con ambas implementaciones sin cambiar
  su código, solo la instancia de `Notificador` que recibe por constructor.
- La solución explica, en una o dos frases, por qué esto es a la vez polimorfismo
  (dos implementaciones de una misma interfaz) e inyección de dependencias (la
  implementación concreta se decide afuera de `ServicioRecordatorios`).

## 🚧 Restricciones

- No es necesario usar anotaciones de Spring; el ejercicio se resuelve en Java
  puro, ensamblando los objetos a mano en un método `main`.

## 📊 Dificultad

Avanzado

## 🎓 Resultados de aprendizaje

RA-1, RA-8

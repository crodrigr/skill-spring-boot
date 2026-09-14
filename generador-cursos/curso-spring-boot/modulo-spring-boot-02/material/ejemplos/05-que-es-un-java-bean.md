# 💡 Ejemplo 05 — ¿Qué es un Java Bean?

## 🌍 Contexto

Un **Java Bean**, en el contexto de Spring, es un objeto administrado, creado
y controlado por el contenedor de Spring. Ya usaste varios sin llamarlos así:
cada clase anotada `@Component`, `@Service`, `@Repository` o `@Controller` que
construiste en el Módulo 1 es un Java Bean. Este bloque formaliza qué
características debería tener una clase para funcionar bien como bean, antes
de profundizar en su ciclo de vida y en `@Component` en los siguientes dos
bloques.

**Qué busca demostrar este ejemplo**: que "ser un Java Bean" no es solo
"tener una anotación de Spring encima": es cumplir un conjunto de
convenciones (propiedades con *getters*/*setters*, capacidad de
serializarse, etc.) que hacen que un objeto sea fácil de reutilizar,
inspeccionar y administrar — con o sin Spring de por medio.

## 🏥 Caso de estudio

MediSalud: una clase `DatosContactoPaciente` evaluada contra las
características de un Java Bean.

## 🔍 Características de un Java Bean

| Característica | Qué significa |
|---|---|
| **Reutilizable** | Se puede usar en distintas aplicaciones sin modificarlo. |
| **Manipulable visualmente** | Puede inspeccionarse y configurarse desde herramientas de desarrollo (IDEs). |
| **Serializable** | Puede convertirse en una secuencia de bytes, para almacenarse o transmitirse por red. |
| **Con propiedades** | Encapsula datos (y comportamiento) mediante propiedades, de solo lectura o de lectura/escritura. |
| **Con métodos *getter*/*setter*** | Expone sus propiedades mediante métodos de acceso, no mediante campos públicos. |
| **Con capacidad de generar eventos** | Puede notificar a otros componentes cuando cambia su estado. |
| **Con capacidad de introspección** | Herramientas externas pueden examinar sus propiedades y métodos automáticamente. |

## 🌳 Árbol de archivos (como se vería en VS Code)

```text
📁 ejemplo-05-que-es-un-java-bean
└── 📁 src
    ├── 📄 DatosContactoPaciente.java  (Java Bean clásico)
    ├── 📄 ServicioContacto.java       (bean administrado por Spring)
    └── 📄 Main.java                   (▶️ clase con el main que se ejecuta)
```

## 💻 Archivo: `DatosContactoPaciente.java`

```java
public class DatosContactoPaciente implements Serializable {

    private String telefono;
    private String email;

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
```

## 💻 Archivo: `ServicioContacto.java`

```java
@Component // convierte la instancia administrada por Spring en un bean del contenedor
public class ServicioContacto {

    public DatosContactoPaciente construirContacto(String telefono, String email) {
        DatosContactoPaciente datos = new DatosContactoPaciente();
        datos.setTelefono(telefono);
        datos.setEmail(email);
        return datos;
    }
}
```

## 💻 Archivo: `Main.java` (▶️ clic derecho → "Run Java" en VS Code)

```java
public class Main {
    public static void main(String[] args) {
        ServicioContacto servicioContacto = new ServicioContacto();
        DatosContactoPaciente contacto = servicioContacto.construirContacto("+54 11 5555-0100", "ana@mail.com");
        System.out.println(contacto.getTelefono());
    }
}
```

## 🧭 Explicación paso a paso

1. `DatosContactoPaciente` cumple las características de un Java Bean
   "clásico" (en el sentido original de la especificación JavaBeans): tiene
   propiedades privadas (`telefono`, `email`) expuestas mediante
   *getters*/*setters*, e implementa `Serializable`.
2. `ServicioContacto`, en cambio, es un **bean administrado por Spring**
   (gracias a `@Component`): el contenedor lo crea y lo entrega a quien lo
   necesite, pero no tiene propiedades de lectura/escritura como
   `DatosContactoPaciente` — su responsabilidad es otra (construir el objeto
   de contacto).
3. Esto muestra que "Java Bean" y "bean administrado por Spring" son
   conceptos relacionados, pero no idénticos: cualquier clase puede ser un
   bean administrado por Spring (con `@Component` u otra anotación), tenga o
   no la forma clásica de propiedades con *getters*/*setters*; y una clase con
   esa forma clásica no necesita `@Component` para ser un Java Bean en el
   sentido general.
4. La **introspección** (que herramientas externas examinen las propiedades
   de una clase automáticamente) es lo que permite, por ejemplo, que un
   framework detecte que `DatosContactoPaciente` tiene una propiedad
   `telefono` con su `getTelefono()`/`setTelefono(...)` correspondiente, sin
   que nadie se lo indique explícitamente.

## ✅ Resultado esperado

Al ejecutar `Main.java`:

```text
+54 11 5555-0100
```

## ❓ Preguntas de repaso

**1. [Selección]** ¿Cuál de estas es una característica de un Java Bean?

- **A.** Debe heredar obligatoriamente de una clase abstracta de Spring.
- **B.** Expone sus propiedades mediante métodos *getter*/*setter*, no mediante campos públicos.
- **C.** No puede tener más de tres propiedades.
- **D.** Solo puede usarse dentro de un contenedor Spring.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuesta correcta: B**. Esa es la convención central de propiedades de un
Java Bean; las otras tres son falsas.

</details>

**2. [Selección múltiple]** Seleccioná **todas** las características de un
Java Bean mencionadas en este ejemplo.

- **A.** Serializable.
- **B.** Manipulable visualmente en un IDE.
- **C.** Debe implementar obligatoriamente una interfaz de Spring.
- **D.** Con capacidad de introspección.

<details>
<summary>🔑 Ver respuesta</summary>

**Respuestas correctas: A, B, D**. La C es falsa: ninguna de las
características de un Java Bean exige implementar una interfaz de Spring.

</details>

**3. [Abierta]** ¿Es lo mismo "ser un Java Bean" que "ser un bean administrado
por Spring"? Explicá la diferencia usando `DatosContactoPaciente` y
`ServicioContacto` de este ejemplo.

<details>
<summary>🔑 Ver respuesta modelo</summary>

**Respuesta modelo**: No son lo mismo, aunque están relacionados.
`DatosContactoPaciente` es un Java Bean en el sentido clásico (propiedades con
*getters*/*setters*, serializable), pero no está anotado con `@Component`, así
que Spring no lo administra: cada uno se crea con `new`, cuando se necesita.
`ServicioContacto` sí es un bean **administrado por Spring** (gracias a
`@Component`), pero no tiene la forma clásica de propiedades de un Java Bean.
Un objeto puede ser una cosa, la otra, ambas, o ninguna.

</details>

SafeRoad

SafeRoad es una aplicación móvil desarrollada en Kotlin utilizando Jetpack Compose que permite gestionar “carreras” de running. La aplicación distingue dos tipos de usuarios (administradores y participantes) y utiliza la suite de servicios de Firebase para autenticación, almacenamiento de datos en Firestore y actualización de la posición en tiempo real mediante Realtime Database.

Funciones principales
Registro de usuarios y gestión de perfiles

Registro: la pantalla de registro solicita nombre, fecha de nacimiento, peso, correo y contraseña, y utiliza FirebaseAuth.createUserWithEmailAndPassword para crear la cuenta; a continuación guarda un objeto Profile con los datos del usuario en la colección profile de Firestore
raw.githubusercontent.com
. El campo role de cada perfil se establece en "user" por defecto
raw.githubusercontent.com
.

Inicio de sesión: la pantalla de login recibe correo y contraseña, e invoca signInWithEmailAndPassword. Una vez autenticado, se obtiene el documento del perfil y se consulta el campo role. Según el rol (usuario o administrador) la navegación se dirige a la pantalla de inicio o al panel de administrador
raw.githubusercontent.com
.

Gestión de perfil: la pantalla de perfil muestra nombre, edad (calculada a partir de la fecha de nacimiento), peso, correo y rol. Permite editar datos o eliminar la cuenta; para eliminarla se solicita la contraseña, se re‑autentica al usuario y luego se borran el documento profile y la cuenta en FirebaseAuth
raw.githubusercontent.com
. También existe una pantalla de edición de perfil (no mostrada aquí) para actualizar los datos básicos.

Home y exploración de carreras

Pantalla de inicio (usuarios): muestra un listado horizontal (LazyRow) de todas las carreras almacenadas en la colección carreras. Cada tarjeta presenta el nombre, la descripción y la imagen codificada en Base64 de la carrera
raw.githubusercontent.com
. Al pulsar una tarjeta se navega a la pantalla de detalles.

Autonavegación a carrera activa: al entrar en la pantalla de inicio se consulta Firestore para detectar si alguna carrera ha comenzado (isStarted = true) y si el usuario está inscrito; en caso afirmativo se redirige automáticamente a la pantalla de inicio de la carrera
raw.githubusercontent.com
.

Pantalla de inicio (administradores): similar a la pantalla de usuario, pero incluye un botón flotante que permite crear nuevas carreras. Se accede sólo cuando el perfil tiene rol admin.

Gestión de carreras por parte del administrador

Los administradores pueden crear, editar y eliminar carreras:

Creación de carreras: el formulario CarreraFormScreen solicita nombre, descripción, imagen, un límite opcional de participantes y permite elegir la ruta sobre un mapa. El formulario emplea un ActivityResult para seleccionar una imagen y guarda la foto codificada en Base64; el administrador puede activar un interruptor para limitar las inscripciones y establecer un número máximo. Para definir la ruta, se navega a la pantalla “Definir ruta” donde cada toque sobre el mapa registra un punto con su calidad de red y traza polilíneas de color (verde, amarillo o rojo) según la calidad
raw.githubusercontent.com
. Al guardar, la ruta se almacena como lista de objetos con latitud, longitud y calidad, y se crea un documento carreras en Firestore con todos los datos
raw.githubusercontent.com
.

Editar carreras: la pantalla de edición permite cambiar nombre, descripción e imagen de la carrera y actualiza el documento en Firestore
raw.githubusercontent.com
.

Eliminar carreras: desde la pantalla de detalles se muestra un menú de configuración si el usuario administrador es creador de la carrera. El administrador puede borrar la carrera, lo que elimina también todas las inscripciones asociadas
raw.githubusercontent.com
.

Inscripción y detalles de una carrera

Pantalla de detalles: muestra la información completa de la carrera (imagen, descripción, creador, cantidad de inscritos y límite). También visualiza una vista previa de la ruta con los colores de calidad de red y permite inscribirse si el usuario no está ya inscrito. Al pulsar el botón Inscribirme, el código comprueba si la carrera tiene cupo y registra al usuario en la subcolección inscripciones; además actualiza el campo carreraActiva del perfil para indicar que el usuario tiene una carrera en curso
raw.githubusercontent.com
. Cuando una carrera ya ha empezado y el usuario está inscrito, se muestra un botón para abrir el mapa completo de la carrera.

Ruta y seguimiento en tiempo real

El proyecto integra Google Maps y los servicios de ubicación de Android para monitorizar y mostrar la ruta de la carrera y la posición de los corredores:

Definición de ruta con calidad de red: la pantalla DefinirRutaCarreraScreen permite al administrador trazar la ruta tocando sobre el mapa. Cada segmento se registra con la calidad de red (CalidadRed.BUENA, MEDIA o MALA) y se dibuja con colores verde, amarillo o rojo respectivamente
raw.githubusercontent.com
raw.githubusercontent.com
.

Mapa de carrera: durante una carrera, el mapa muestra la ruta coloreada según la calidad y marca el inicio y fin. Además lee la posición de cada corredor desde Realtime Database y coloca un marcador de un color aleatorio por corredor. Se incluye un campo de búsqueda para filtrar corredores por nombre (archivo no mostrado). El mapa utiliza CameraPosition y un estilo personalizado para mejorar la legibilidad.

Inicio de carrera para participantes: en la pantalla ParticipanteCarreraStartScreen el corredor concede permisos de ubicación y luego puede pulsar “Comenzar carrera”. La aplicación inicia un ForegroundService que envía la localización a la base de datos cada 5 segundos. Mientras la carrera está activa, la interfaz muestra la distancia restante, el progreso (barras de avance), el tiempo total, la velocidad media y una estimación de calorías, y permite abandonar la carrera cuando finalice
raw.githubusercontent.com
.

Arquitectura y tecnologías empleadas

Jetpack Compose: toda la interfaz está escrita con composables y se gestiona la navegación mediante NavHost y objetos serializables para las rutas
raw.githubusercontent.com
. Hay barras de navegación inferiores diferenciadas para usuarios y administradores
raw.githubusercontent.com
.

Firebase:

Autenticación: manejo de registro e inicio de sesión con correo y contraseña.

Firestore: almacena perfiles de usuarios y documentos de carreras, incluidas las rutas y los límites de inscripción.

Realtime Database: guarda las posiciones en tiempo real de los corredores durante la carrera.

Google Maps y servicios de ubicación: se usan las bibliotecas Google Maps Compose y FusedLocationProviderClient para mostrar mapas, seleccionar rutas y obtener la ubicación del dispositivo.

Gestión de roles: se diferencia entre usuarios y administradores mediante un campo role en el perfil. El flujo de navegación inicial decide la pantalla de inicio en función de ese rol
raw.githubusercontent.com
.

Cómo ejecutar el proyecto

Clona este repositorio y ábrelo con Android Studio.

Configura un proyecto de Firebase y descarga el archivo google-services.json dentro del módulo app para habilitar la autenticación y la base de datos.

Activa Maps SDK for Android en tu consola de Google Cloud y añade la clave de API en el archivo local.properties (MAPS_API_KEY=YOUR_KEY).

Ejecuta la aplicación en un emulador o dispositivo físico con los servicios de Google Play instalados.

Créditos y licencia

SafeRoad es un proyecto académico/desarrollado con fines de aprendizaje. No se declara una licencia explícita en el repositorio. Si deseas contribuir, abre un pull request o crea un issue en GitHub.

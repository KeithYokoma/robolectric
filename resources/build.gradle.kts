plugins {
  alias(libs.plugins.robolectric.deployed.java.module)
  alias(libs.plugins.robolectric.java.module)
}

dependencies {
  api(project(":utils"))
  api(project(":pluginapi"))

  api(libs.auto.value.annotations)
  api(libs.guava)
  compileOnly(libs.findbugs.jsr305)

  testImplementation(libs.junit4)
  testImplementation(libs.truth)
  testImplementation(libs.mockito)
  testImplementation(libs.mockito.subclass)

  annotationProcessor(libs.auto.value)
}

package org.robolectric.plugins;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.robolectric.annotation.Config.DEFAULT_APPLICATION;

import android.app.Application;
import com.google.common.collect.ImmutableMap;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.TestFakeApp;
import org.robolectric.annotation.Config;
import org.robolectric.pluginapi.config.ConfigurationStrategy;
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration;
import org.robolectric.pluginapi.config.Configurer;
import org.robolectric.shadows.ShadowView;
import org.robolectric.shadows.ShadowViewGroup;
import org.robolectric.shadows.testing.TestApplication;

@RunWith(JUnit4.class)
public class HierarchicalConfigurationStrategyTest {

  @Test
  public void defaultValuesAreMerged() {
    assertThat(configFor(Test2.class, "withoutAnnotation").manifest())
        .isEqualTo("AndroidManifest.xml");
  }

  @Test
  public void globalValuesAreMerged() {
    assertThat(
            configFor(
                    Test2.class,
                    "withoutAnnotation",
                    new Config.Builder().setManifest("ManifestFromGlobal.xml").build())
                .manifest())
        .isEqualTo("ManifestFromGlobal.xml");
  }

  @Test
  public void whenClassHasConfigAnnotation_getConfig_shouldMergeClassAndMethodConfig() {
    assertConfig(
        configFor(Test1.class, "withoutAnnotation"),
        new int[] {1},
        "foo",
        TestFakeApp.class,
        "from-test",
        new Class<?>[] {Test1.class},
        new String[] {"com.example.test1"});

    assertConfig(
        configFor(Test1.class, "withDefaultsAnnotation"),
        new int[] {1},
        "foo",
        TestFakeApp.class,
        "from-test",
        new Class<?>[] {Test1.class},
        new String[] {"com.example.test1"});

    assertConfig(
        configFor(Test1.class, "withOverrideAnnotation"),
        new int[] {9},
        "furf",
        TestApplication.class,
        "from-method",
        new Class<?>[] {Test1.class, Test2.class},
        new String[] {"com.example.test1", "com.example.method1"});
  }

  @Test
  public void whenClassDoesntHaveConfigAnnotation_getConfig_shouldUseMethodConfig() {
    assertConfig(
        configFor(Test2.class, "withoutAnnotation"),
        new int[0],
        "AndroidManifest.xml",
        DEFAULT_APPLICATION,
        "",
        new Class<?>[] {},
        new String[] {});

    assertConfig(
        configFor(Test2.class, "withDefaultsAnnotation"),
        new int[0],
        "AndroidManifest.xml",
        DEFAULT_APPLICATION,
        "",
        new Class<?>[] {},
        new String[] {});

    assertConfig(
        configFor(Test2.class, "withOverrideAnnotation"),
        new int[] {9},
        "furf",
        TestFakeApp.class,
        "from-method",
        new Class<?>[] {Test1.class},
        new String[] {"com.example.method2"});
  }

  @Test
  public void
      whenClassDoesntHaveConfigAnnotation_getConfig_shouldMergeParentClassAndMethodConfig() {
    assertConfig(
        configFor(Test1B.class, "withoutAnnotation"),
        new int[] {1},
        "foo",
        TestFakeApp.class,
        "from-test",
        new Class<?>[] {Test1.class, Test1.class},
        new String[] {"com.example.test1"});

    assertConfig(
        configFor(Test1B.class, "withDefaultsAnnotation"),
        new int[] {1},
        "foo",
        TestFakeApp.class,
        "from-test",
        new Class<?>[] {Test1.class, Test1.class},
        new String[] {"com.example.test1"});

    assertConfig(
        configFor(Test1B.class, "withOverrideAnnotation"),
        new int[] {14},
        "foo",
        TestFakeApp.class,
        "from-method5",
        new Class<?>[] {Test1.class, Test1.class, Test1B.class},
        new String[] {"com.example.test1", "com.example.method5"});
  }

  @Test
  public void
      whenClassAndParentClassHaveConfigAnnotation_getConfig_shouldMergeParentClassAndMethodConfig() {
    assertConfig(
        configFor(Test1C.class, "withoutAnnotation"),
        new int[] {1},
        "foo",
        TestFakeApp.class,
        "from-class6",
        new Class<?>[] {Test1.class, Test1.class, Test1C.class},
        new String[] {"com.example.test1", "com.example.test6"});

    assertConfig(
        configFor(Test1C.class, "withDefaultsAnnotation"),
        new int[] {1},
        "foo",
        TestFakeApp.class,
        "from-class6",
        new Class<?>[] {Test1.class, Test1.class, Test1C.class},
        new String[] {"com.example.test1", "com.example.test6"});

    assertConfig(
        configFor(Test1C.class, "withOverrideAnnotation"),
        new int[] {14},
        "foo",
        TestFakeApp.class,
        "from-method5",
        new Class<?>[] {Test1.class, Test1.class, Test1C.class, Test1B.class},
        new String[] {"com.example.test1", "com.example.method5", "com.example.test6"});
  }

  @Test
  public void
      whenClassAndSubclassHaveConfigAnnotation_getConfig_shouldMergeClassSubclassAndMethodConfig() {
    assertConfig(
        configFor(Test1A.class, "withoutAnnotation"),
        new int[] {1},
        "foo",
        TestFakeApp.class,
        "from-subclass",
        new Class<?>[] {Test1.class},
        new String[] {"com.example.test1"});

    assertConfig(
        configFor(Test1A.class, "withDefaultsAnnotation"),
        new int[] {1},
        "foo",
        TestFakeApp.class,
        "from-subclass",
        new Class<?>[] {Test1.class},
        new String[] {"com.example.test1"});

    assertConfig(
        configFor(Test1A.class, "withOverrideAnnotation"),
        new int[] {9},
        "furf",
        TestApplication.class,
        "from-method",
        new Class<?>[] {Test1.class, Test2.class},
        new String[] {"com.example.test1", "com.example.method1"});
  }

  @Test
  public void
      whenClassDoesntHaveConfigAnnotationButSubclassDoes_getConfig_shouldMergeSubclassAndMethodConfig() {
    assertConfig(
        configFor(Test2A.class, "withoutAnnotation"),
        new int[0],
        "AndroidManifest.xml",
        DEFAULT_APPLICATION,
        "from-subclass",
        new Class<?>[] {},
        new String[] {});

    assertConfig(
        configFor(Test2A.class, "withDefaultsAnnotation"),
        new int[0],
        "AndroidManifest.xml",
        DEFAULT_APPLICATION,
        "from-subclass",
        new Class<?>[] {},
        new String[] {});

    assertConfig(
        configFor(Test2A.class, "withOverrideAnnotation"),
        new int[] {9},
        "furf",
        TestFakeApp.class,
        "from-method",
        new Class<?>[] {Test1.class},
        new String[] {"com.example.method2"});
  }

  @Test
  public void shouldLoadDefaultsFromGlobalPropertiesFile() {
    String properties =
        "sdk: 432\n"
            + "manifest: --none\n"
            + "qualifiers: from-properties-file\n"
            + "resourceDir: from/properties/file/res\n"
            + "assetDir: from/properties/file/assets\n"
            + "shadows: org.robolectric.shadows.ShadowView,"
            + " org.robolectric.shadows.ShadowViewGroup\n"
            + "application: org.robolectric.TestFakeApp\n"
            + "packageName: com.example.test\n"
            + "instrumentedPackages: com.example.test1, com.example.test2\n"
            + "libraries: libs/test, libs/test2";

    assertConfig(
        configFor(
            Test2.class,
            "withoutAnnotation",
            ImmutableMap.of("robolectric.properties", properties)),
        new int[] {432},
        "--none",
        TestFakeApp.class,
        "from-properties-file",
        new Class<?>[] {ShadowView.class, ShadowViewGroup.class},
        new String[] {"com.example.test1", "com.example.test2"});
  }

  @Test
  public void shouldMergeConfigFromTestClassPackageProperties() {
    assertConfig(
        configFor(
            Test2.class,
            "withoutAnnotation",
            ImmutableMap.of("org/robolectric/robolectric.properties", "sdk: 432\n")),
        new int[] {432},
        "AndroidManifest.xml",
        DEFAULT_APPLICATION,
        "",
        new Class<?>[] {},
        new String[] {});
  }

  @Test
  public void shouldMergeConfigUpPackageHierarchy() {
    assertConfig(
        configFor(
            Test2.class,
            "withoutAnnotation",
            ImmutableMap.of(
                "org/robolectric/robolectric.properties",
                    "qualifiers: from-org-robolectric\nlibraries: FromOrgRobolectric\n",
                "org/robolectric.properties",
                    "sdk: 123\nqualifiers: from-org\nlibraries: FromOrg\n",
                "robolectric.properties",
                    "sdk: 456\nqualifiers: from-top-level\nlibraries: FromTopLevel\n")),
        new int[] {123},
        "AndroidManifest.xml",
        DEFAULT_APPLICATION,
        "from-org-robolectric",
        new Class<?>[] {},
        new String[] {});
  }

  @Test
  public void withEmptyShadowList_shouldLoadDefaultsFromGlobalPropertiesFile() {
    assertConfig(
        configFor(
            Test2.class,
            "withoutAnnotation",
            ImmutableMap.of("robolectric.properties", "shadows:")),
        new int[0],
        "AndroidManifest.xml",
        DEFAULT_APPLICATION,
        "",
        new Class<?>[] {},
        new String[] {});
  }

  @Test
  public void testPrecedence() throws Exception {
    SpyConfigurer spyConfigurer = new SpyConfigurer();

    ConfigurationStrategy configStrategy = new HierarchicalConfigurationStrategy(spyConfigurer);

    assertThat(computeConfig(configStrategy, Test1.class, "withoutAnnotation"))
        .isEqualTo(
            "default:(top):org:org.robolectric:org.robolectric.plugins"
                + ":"
                + Test1.class.getName()
                + ":withoutAnnotation");

    assertThat(computeConfig(configStrategy, Test1A.class, "withOverrideAnnotation"))
        .isEqualTo(
            "default:(top):org:org.robolectric:org.robolectric.plugins"
                + ":"
                + Test1.class.getName()
                + ":"
                + Test1A.class.getName()
                + ":withOverrideAnnotation");
  }

  @Test
  public void testTestClassMatters() throws Exception {
    SpyConfigurer spyConfigurer = new SpyConfigurer();

    ConfigurationStrategy configStrategy = new HierarchicalConfigurationStrategy(spyConfigurer);

    assertThat(computeConfig(configStrategy, Test1.class, "withoutAnnotation"))
        .isEqualTo(
            "default:(top):org:org.robolectric:org.robolectric.plugins"
                + ":"
                + Test1.class.getName()
                + ":withoutAnnotation");

    assertThat(computeConfig(configStrategy, Test1A.class, "withoutAnnotation"))
        .isEqualTo(
            "default:(top):org:org.robolectric:org.robolectric.plugins"
                + ":"
                + Test1.class.getName()
                + ":"
                + Test1A.class.getName()
                + ":withoutAnnotation");
  }

  @Test
  public void testBigOAndCaching() throws Exception {
    SpyConfigurer spyConfigurer = new SpyConfigurer();
    ConfigurationStrategy configStrategy = new HierarchicalConfigurationStrategy(spyConfigurer);
    computeConfig(configStrategy, Test1A.class, "withoutAnnotation");

    assertThat(spyConfigurer.log)
        .containsExactly(
            "default",
            "withoutAnnotation",
            Test1A.class.getName(),
            Test1.class.getName(),
            "org.robolectric.plugins",
            "org.robolectric",
            "org",
            "(top)")
        .inOrder();

    spyConfigurer.log.clear();
    computeConfig(configStrategy, Test1.class, "withoutAnnotation");
    assertThat(spyConfigurer.log).containsExactly("withoutAnnotation").inOrder();

    spyConfigurer.log.clear();
    computeConfig(configStrategy, Test2A.class, "withOverrideAnnotation");
    assertThat(spyConfigurer.log)
        .containsExactly("withOverrideAnnotation", Test2A.class.getName(), Test2.class.getName())
        .inOrder();
  }

  /////////////////////////////

  private String computeConfig(
      ConfigurationStrategy configStrategy, Class<?> testClass, String methodName)
      throws NoSuchMethodException {
    return configStrategy.getConfig(testClass, testClass.getMethod(methodName)).get(String.class);
  }

  private Config configFor(
      Class<?> testClass, String methodName, final Map<String, String> configProperties) {
    return configFor(testClass, methodName, configProperties, null);
  }

  private Config configFor(Class<?> testClass, String methodName) {
    Config.Implementation globalConfig = Config.Builder.defaults().build();
    return configFor(testClass, methodName, globalConfig);
  }

  private Config configFor(
      Class<?> testClass, String methodName, Config.Implementation globalConfig) {
    return configFor(testClass, methodName, new HashMap<>(), globalConfig);
  }

  private Config configFor(
      Class<?> testClass,
      String methodName,
      final Map<String, String> configProperties,
      Config.Implementation globalConfig) {
    Method info = getMethod(testClass, methodName);
    PackagePropertiesLoader packagePropertiesLoader =
        new PackagePropertiesLoader() {
          @Override
          InputStream getResourceAsStream(String resourceName) {
            String properties = configProperties.get(resourceName);
            return properties == null ? null : new ByteArrayInputStream(properties.getBytes(UTF_8));
          }
        };
    ConfigurationStrategy defaultConfigStrategy =
        new HierarchicalConfigurationStrategy(
            new ConfigConfigurer(
                packagePropertiesLoader,
                () -> globalConfig == null ? Config.Builder.defaults().build() : globalConfig));
    Configuration config = defaultConfigStrategy.getConfig(testClass, info);
    return config.get(Config.class);
  }

  private static Method getMethod(Class<?> testClass, String methodName) {
    try {
      return testClass.getMethod(methodName);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private static void assertConfig(
      Config config,
      int[] sdk,
      String manifest,
      Class<? extends Application> application,
      String qualifiers,
      Class<?>[] shadows,
      String[] instrumentedPackages) {
    assertThat(config.sdk()).isEqualTo(sdk);
    assertThat(config.manifest()).isEqualTo(manifest);
    assertThat(config.application()).isEqualTo(application);
    assertThat(config.qualifiers()).isEqualTo(qualifiers);
    assertThat(config.shadows()).asList().containsAtLeastElementsIn(shadows).inOrder();
    assertThat(config.instrumentedPackages())
        .asList()
        .containsAtLeastElementsIn(instrumentedPackages);
  }

  @Ignore
  @Config(
      sdk = 1,
      manifest = "foo",
      application = TestFakeApp.class,
      shadows = Test1.class,
      instrumentedPackages = "com.example.test1",
      qualifiers = "from-test")
  public static class Test1 {
    @Test
    public void withoutAnnotation() {}

    @Test
    @Config
    public void withDefaultsAnnotation() {}

    @Test
    @Config(
        sdk = 9,
        manifest = "furf",
        application = TestApplication.class,
        shadows = Test2.class,
        instrumentedPackages = "com.example.method1",
        qualifiers = "from-method")
    public void withOverrideAnnotation() {}
  }

  @Ignore
  public static class Test2 {
    @Test
    public void withoutAnnotation() {}

    @Test
    @Config
    public void withDefaultsAnnotation() {}

    @Test
    @Config(
        sdk = 9,
        manifest = "furf",
        application = TestFakeApp.class,
        shadows = Test1.class,
        instrumentedPackages = "com.example.method2",
        qualifiers = "from-method")
    public void withOverrideAnnotation() {}
  }

  @Ignore
  @Config(qualifiers = "from-subclass")
  public static class Test1A extends Test1 {}

  @Ignore
  @Config(qualifiers = "from-subclass")
  public static class Test2A extends Test2 {}

  @Ignore
  public static class Test1B extends Test1 {
    @Override
    @Test
    public void withoutAnnotation() {}

    @Override
    @Test
    @Config
    public void withDefaultsAnnotation() {}

    @Override
    @Test
    @Config(
        sdk = 14,
        shadows = Test1B.class,
        instrumentedPackages = "com.example.method5",
        qualifiers = "from-method5")
    public void withOverrideAnnotation() {}
  }

  @Ignore
  @Config(
      qualifiers = "from-class6",
      shadows = Test1C.class,
      instrumentedPackages = "com.example.test6")
  public static class Test1C extends Test1B {}

  private static class SpyConfigurer implements Configurer<String> {

    final List<String> log = new ArrayList<>();

    @Override
    public Class<String> getConfigClass() {
      return String.class;
    }

    @Nonnull
    @Override
    public String defaultConfig() {
      return log("default");
    }

    @Override
    public String getConfigFor(@Nonnull String packageName) {
      return log(packageName.isEmpty() ? "(top)" : packageName);
    }

    @Override
    public String getConfigFor(@Nonnull Class<?> testClass) {
      return log(testClass.getName());
    }

    @Override
    public String getConfigFor(@Nonnull Method method) {
      return log(method.getName());
    }

    @Nonnull
    @Override
    public String merge(@Nonnull String parentConfig, @Nonnull String childConfig) {
      return parentConfig + ":" + childConfig;
    }

    private String log(String s) {
      log.add(s);
      return s;
    }
  }
}

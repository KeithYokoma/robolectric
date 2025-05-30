package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.app.Activity;
import android.app.LocaleManager;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.os.LocaleList;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.junit.rules.SetSystemPropertyRule;
import org.robolectric.shadow.api.Shadow;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VERSION_CODES.TIRAMISU)
public final class ShadowLocaleManagerTest {
  private static final String DEFAULT_PACKAGE_NAME = "my.app";
  private static final LocaleList DEFAULT_LOCALES = LocaleList.forLanguageTags("en-XC,ar-XB");

  @Rule public SetSystemPropertyRule setSystemPropertyRule = new SetSystemPropertyRule();

  private LocaleManager localeManager;
  private ShadowLocaleManager shadowLocaleManager;

  @Before
  public void setUp() {
    Context context = ApplicationProvider.getApplicationContext();
    localeManager = context.getSystemService(LocaleManager.class);
    shadowLocaleManager = Shadow.extract(localeManager);
  }

  @Test
  public void setApplicationLocales_updatesMap() {
    // empty map before set is called.
    assertThat(localeManager.getApplicationLocales(DEFAULT_PACKAGE_NAME))
        .isEqualTo(LocaleList.getEmptyLocaleList());

    localeManager.setApplicationLocales(DEFAULT_PACKAGE_NAME, DEFAULT_LOCALES);

    shadowLocaleManager.enforceInstallerCheck(false);
    assertThat(localeManager.getApplicationLocales(DEFAULT_PACKAGE_NAME))
        .isEqualTo(DEFAULT_LOCALES);
  }

  @Test
  public void setApplicationLocales_defaultPackage_updatesMap() {
    // empty map before set is called.
    assertThat(localeManager.getApplicationLocales(DEFAULT_PACKAGE_NAME))
        .isEqualTo(LocaleList.getEmptyLocaleList());

    localeManager.setApplicationLocales(DEFAULT_LOCALES);

    shadowLocaleManager.enforceInstallerCheck(false);
    assertThat(localeManager.getApplicationLocales()).isEqualTo(DEFAULT_LOCALES);
  }

  @Test
  public void getApplicationLocales_fetchAsInstaller_returnsLocales() {
    localeManager.setApplicationLocales(DEFAULT_PACKAGE_NAME, DEFAULT_LOCALES);
    shadowLocaleManager.setCallerAsInstallerForPackage(DEFAULT_PACKAGE_NAME);
    shadowLocaleManager.enforceInstallerCheck(true);

    assertThat(localeManager.getApplicationLocales(DEFAULT_PACKAGE_NAME))
        .isEqualTo(DEFAULT_LOCALES);
  }

  @Test
  public void getApplicationLocales_fetchAsInstaller_throwsSecurityExceptionIfIncorrectInstaller() {
    localeManager.setApplicationLocales(DEFAULT_PACKAGE_NAME, DEFAULT_LOCALES);
    shadowLocaleManager.enforceInstallerCheck(true);

    assertThrows(
        SecurityException.class, () -> localeManager.getApplicationLocales(DEFAULT_PACKAGE_NAME));
  }

  @Test
  @Config(qualifiers = "en")
  public void getSystemLocales_en() {
    LocaleList localeList = localeManager.getSystemLocales();
    assertThat(localeList.size()).isEqualTo(1);
    assertThat(localeList.get(0).getLanguage()).isEqualTo("en");
  }

  @Test
  @Config(qualifiers = "zh")
  public void getSystemLocales_zh() {
    LocaleList localeList = localeManager.getSystemLocales();
    assertThat(localeList.size()).isEqualTo(1);
    assertThat(localeList.get(0).getLanguage()).isEqualTo("zh");
  }

  @Test
  public void localeManager_activityContextEnabled_differentInstancesRetrieveLocales() {
    setSystemPropertyRule.set("robolectric.createActivityContexts", "true");

    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      LocaleManager applicationLocaleManager =
          (LocaleManager)
              ApplicationProvider.getApplicationContext().getSystemService(Context.LOCALE_SERVICE);
      Activity activity = controller.get();
      LocaleManager activityLocaleManager =
          (LocaleManager) activity.getSystemService(Context.LOCALE_SERVICE);

      assertThat(applicationLocaleManager).isNotSameInstanceAs(activityLocaleManager);

      LocaleList applicationLocales = applicationLocaleManager.getApplicationLocales();
      LocaleList activityLocales = activityLocaleManager.getApplicationLocales();

      assertThat(activityLocales).isEqualTo(applicationLocales);
    }
  }
}

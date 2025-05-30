package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.junit.rules.SetSystemPropertyRule;

@RunWith(AndroidJUnit4.class)
public class ShadowSearchManagerTest {
  @Rule public SetSystemPropertyRule setSystemPropertyRule = new SetSystemPropertyRule();

  @Test
  @Config(minSdk = O)
  public void
      searchManager_activityContextEnabled_differentInstancesRetrieveGlobalSearchActivity() {
    setSystemPropertyRule.set("robolectric.createActivityContexts", "true");

    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      SearchManager applicationSearchManager =
          RuntimeEnvironment.getApplication().getSystemService(SearchManager.class);
      Activity activity = controller.get();
      SearchManager activitySearchManager = activity.getSystemService(SearchManager.class);

      assertThat(applicationSearchManager).isNotSameInstanceAs(activitySearchManager);

      ComponentName applicationGlobalSearchActivity =
          applicationSearchManager.getGlobalSearchActivity();
      ComponentName activityGlobalSearchActivity = activitySearchManager.getGlobalSearchActivity();

      assertThat(activityGlobalSearchActivity).isEqualTo(applicationGlobalSearchActivity);
    }
  }
}

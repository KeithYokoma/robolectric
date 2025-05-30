package org.robolectric.shadows;

import static android.content.Context.TELEPHONY_SUBSCRIPTION_SERVICE;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static android.telephony.SubscriptionManager.INVALID_SIM_SLOT_INDEX;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.junit.rules.SetSystemPropertyRule;
import org.robolectric.shadows.ShadowSubscriptionManager.SubscriptionInfoBuilder;

/** Test for {@link ShadowSubscriptionManager}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = N)
public class ShadowSubscriptionManagerTest {
  @Rule public SetSystemPropertyRule setSystemPropertyRule = new SetSystemPropertyRule();

  private SubscriptionManager subscriptionManager;

  @Before
  public void setUp() throws Exception {
    subscriptionManager =
        (SubscriptionManager)
            getApplicationContext().getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE);
  }

  @Config(minSdk = R)
  @Test
  public void shouldGiveActiveDataSubscriptionId() {
    int testId = 42;
    ShadowSubscriptionManager.setActiveDataSubscriptionId(testId);
    assertThat(SubscriptionManager.getActiveDataSubscriptionId()).isEqualTo(testId);
  }

  @Test
  public void shouldGiveDefaultSubscriptionId() {
    int testId = 42;
    ShadowSubscriptionManager.setDefaultSubscriptionId(testId);
    assertThat(SubscriptionManager.getDefaultSubscriptionId()).isEqualTo(testId);
  }

  @Test
  public void shouldGiveDefaultDataSubscriptionId() {
    int testId = 42;
    ShadowSubscriptionManager.setDefaultDataSubscriptionId(testId);
    assertThat(SubscriptionManager.getDefaultDataSubscriptionId()).isEqualTo(testId);
  }

  @Test
  public void shouldGiveDefaultSmsSubscriptionId() {
    int testId = 42;
    ShadowSubscriptionManager.setDefaultSmsSubscriptionId(testId);
    assertThat(SubscriptionManager.getDefaultSmsSubscriptionId()).isEqualTo(testId);
  }

  @Test
  public void shouldGiveDefaultVoiceSubscriptionId() {
    int testId = 42;
    ShadowSubscriptionManager.setDefaultVoiceSubscriptionId(testId);
    assertThat(SubscriptionManager.getDefaultVoiceSubscriptionId()).isEqualTo(testId);
  }

  @Test
  public void addOnSubscriptionsChangedListener_shouldCallbackImmediately() {
    DummySubscriptionsChangedListener listener = new DummySubscriptionsChangedListener();
    shadowOf(subscriptionManager).addOnSubscriptionsChangedListener(listener);

    assertThat(listener.subscriptionChangedCount).isEqualTo(1);
  }

  @Test
  public void addOnSubscriptionsChangedListener_shouldAddListener() {
    DummySubscriptionsChangedListener listener = new DummySubscriptionsChangedListener();
    shadowOf(subscriptionManager).addOnSubscriptionsChangedListener(listener);

    shadowOf(subscriptionManager)
        .setActiveSubscriptionInfos(
            SubscriptionInfoBuilder.newBuilder().setId(123).buildSubscriptionInfo());

    assertThat(listener.subscriptionChangedCount).isEqualTo(2);
  }

  @Test
  public void
      addOnSubscriptionsChangedListener_whenHasExecutorParameter_shouldCallbackImmediately() {
    DummySubscriptionsChangedListener listener = new DummySubscriptionsChangedListener();
    shadowOf(subscriptionManager)
        .addOnSubscriptionsChangedListener(new Handler(Looper.getMainLooper())::post, listener);

    assertThat(listener.subscriptionChangedCount).isEqualTo(1);
  }

  @Test
  public void addOnSubscriptionsChangedListener_whenHasExecutorParameter_shouldAddListener() {
    DummySubscriptionsChangedListener listener = new DummySubscriptionsChangedListener();
    shadowOf(subscriptionManager)
        .addOnSubscriptionsChangedListener(new Handler(Looper.getMainLooper())::post, listener);

    shadowOf(subscriptionManager)
        .setActiveSubscriptionInfos(
            SubscriptionInfoBuilder.newBuilder().setId(123).buildSubscriptionInfo());

    assertThat(listener.subscriptionChangedCount).isEqualTo(2);
  }

  @Test
  public void removeOnSubscriptionsChangedListener_shouldRemoveListener() {
    DummySubscriptionsChangedListener listener = new DummySubscriptionsChangedListener();
    DummySubscriptionsChangedListener listener2 = new DummySubscriptionsChangedListener();
    shadowOf(subscriptionManager).addOnSubscriptionsChangedListener(listener);
    shadowOf(subscriptionManager).addOnSubscriptionsChangedListener(listener2);

    shadowOf(subscriptionManager).removeOnSubscriptionsChangedListener(listener);
    shadowOf(subscriptionManager)
        .setActiveSubscriptionInfos(
            SubscriptionInfoBuilder.newBuilder().setId(123).buildSubscriptionInfo());

    assertThat(listener.subscriptionChangedCount).isEqualTo(1);
    assertThat(listener2.subscriptionChangedCount).isEqualTo(2);
  }

  @Test
  public void hasOnSubscriptionsChangedListener_whenListenerNotExist_shouldReturnFalse() {
    DummySubscriptionsChangedListener listener = new DummySubscriptionsChangedListener();

    assertThat(shadowOf(subscriptionManager).hasOnSubscriptionsChangedListener(listener)).isFalse();
  }

  @Test
  public void hasOnSubscriptionsChangedListener_whenListenerExist_shouldReturnTrue() {
    DummySubscriptionsChangedListener listener = new DummySubscriptionsChangedListener();
    shadowOf(subscriptionManager).addOnSubscriptionsChangedListener(listener);

    assertThat(shadowOf(subscriptionManager).hasOnSubscriptionsChangedListener(listener)).isTrue();
  }

  @Test
  public void getActiveSubscriptionInfo_shouldReturnInfoWithSubId() {
    SubscriptionInfo expectedSubscriptionInfo =
        SubscriptionInfoBuilder.newBuilder().setId(123).buildSubscriptionInfo();
    shadowOf(subscriptionManager).setActiveSubscriptionInfos(expectedSubscriptionInfo);

    assertThat(shadowOf(subscriptionManager).getActiveSubscriptionInfo(123))
        .isSameInstanceAs(expectedSubscriptionInfo);
  }

  @Test
  public void getActiveSubscriptionInfo_shouldThrowExceptionWhenNoPermissions() {
    shadowOf(subscriptionManager).setReadPhoneStatePermission(false);
    assertThrows(
        SecurityException.class,
        () ->
            shadowOf(subscriptionManager)
                .getActiveSubscriptionInfo(SubscriptionManager.DEFAULT_SUBSCRIPTION_ID));
  }

  @Test
  public void getActiveSubscriptionInfoList_shouldReturnInfoList() {
    SubscriptionInfo expectedSubscriptionInfo =
        SubscriptionInfoBuilder.newBuilder().setId(123).buildSubscriptionInfo();
    shadowOf(subscriptionManager).setActiveSubscriptionInfos(expectedSubscriptionInfo);

    assertThat(shadowOf(subscriptionManager).getActiveSubscriptionInfoList())
        .containsExactly(expectedSubscriptionInfo);
  }

  @Test
  public void getActiveSubscriptionInfoList_shouldThrowExceptionWhenNoPermissions() {
    shadowOf(subscriptionManager).setReadPhoneStatePermission(false);
    assertThrows(
        SecurityException.class,
        () -> shadowOf(subscriptionManager).getActiveSubscriptionInfoList());
  }

  @Test
  public void getActiveSubscriptionInfoForSimSlotIndex_shouldReturnInfoWithSlotIndex() {
    SubscriptionInfo expectedSubscriptionInfo =
        SubscriptionInfoBuilder.newBuilder().setSimSlotIndex(123).buildSubscriptionInfo();
    shadowOf(subscriptionManager).setActiveSubscriptionInfos(expectedSubscriptionInfo);

    assertThat(shadowOf(subscriptionManager).getActiveSubscriptionInfoForSimSlotIndex(123))
        .isSameInstanceAs(expectedSubscriptionInfo);
  }

  @Test
  public void getActiveSubscriptionInfoForSimSlotIndex_shouldThrowExceptionWhenNoPermissions() {
    shadowOf(subscriptionManager).setReadPhoneStatePermission(false);
    assertThrows(
        SecurityException.class,
        () -> shadowOf(subscriptionManager).getActiveSubscriptionInfoForSimSlotIndex(123));
  }

  @Test
  public void getActiveSubscriptionInfo_shouldReturnNullForNullList() {
    shadowOf(subscriptionManager).setActiveSubscriptionInfoList(null);
    assertThat(shadowOf(subscriptionManager).getActiveSubscriptionInfo(123)).isNull();
  }

  @Test
  public void getActiveSubscriptionInfo_shouldReturnNullForNullVarargsList() {
    shadowOf(subscriptionManager).setActiveSubscriptionInfos((SubscriptionInfo[]) null);
    assertThat(shadowOf(subscriptionManager).getActiveSubscriptionInfo(123)).isNull();
  }

  @Test
  public void getActiveSubscriptionInfo_shouldReturnNullForEmptyList() {
    shadowOf(subscriptionManager).setActiveSubscriptionInfos();
    assertThat(shadowOf(subscriptionManager).getActiveSubscriptionInfo(123)).isNull();
  }

  @Test
  public void isNetworkRoaming_shouldReturnTrueIfSet() {
    shadowOf(subscriptionManager).setNetworkRoamingStatus(123, /* isNetworkRoaming= */ true);
    assertThat(shadowOf(subscriptionManager).isNetworkRoaming(123)).isTrue();
  }

  /** Multi act-asserts are discouraged but here we are testing the set+unset. */
  @Test
  public void isNetworkRoaming_shouldReturnFalseIfUnset() {
    shadowOf(subscriptionManager).setNetworkRoamingStatus(123, /* isNetworkRoaming= */ true);
    assertThat(shadowOf(subscriptionManager).isNetworkRoaming(123)).isTrue();

    shadowOf(subscriptionManager).setNetworkRoamingStatus(123, /* isNetworkRoaming= */ false);
    assertThat(shadowOf(subscriptionManager).isNetworkRoaming(123)).isFalse();
  }

  /** Multi act-asserts are discouraged but here we are testing the set+clear. */
  @Test
  public void isNetworkRoaming_shouldReturnFalseOnClear() {
    shadowOf(subscriptionManager).setNetworkRoamingStatus(123, /* isNetworkRoaming= */ true);
    assertThat(shadowOf(subscriptionManager).isNetworkRoaming(123)).isTrue();

    shadowOf(subscriptionManager).clearNetworkRoamingStatus();
    assertThat(shadowOf(subscriptionManager).isNetworkRoaming(123)).isFalse();
  }

  @Test
  public void getActiveSubscriptionInfoCount_shouldReturnZeroIfActiveSubscriptionInfoListNotSet() {
    shadowOf(subscriptionManager).setActiveSubscriptionInfoList(null);

    assertThat(shadowOf(subscriptionManager).getActiveSubscriptionInfoCount()).isEqualTo(0);
  }

  @Test
  public void getActiveSubscriptionInfoCount_shouldReturnSizeOfActiveSubscriptionInfosList() {
    SubscriptionInfo expectedSubscriptionInfo =
        SubscriptionInfoBuilder.newBuilder().setId(123).buildSubscriptionInfo();
    shadowOf(subscriptionManager).setActiveSubscriptionInfos(expectedSubscriptionInfo);

    assertThat(shadowOf(subscriptionManager).getActiveSubscriptionInfoCount()).isEqualTo(1);
  }

  @Test
  public void getActiveSubscriptionInfoCountMax_returnsSubscriptionListCount() {
    SubscriptionInfo subscriptionInfo =
        SubscriptionInfoBuilder.newBuilder().setId(123).buildSubscriptionInfo();
    shadowOf(subscriptionManager).setActiveSubscriptionInfos(subscriptionInfo);

    assertThat(subscriptionManager.getActiveSubscriptionInfoCountMax()).isEqualTo(1);
  }

  @Test
  public void getActiveSubscriptionInfoCountMax_nullInfoListIsZero() {
    shadowOf(subscriptionManager).setActiveSubscriptionInfoList(null);

    assertThat(subscriptionManager.getActiveSubscriptionInfoCountMax()).isEqualTo(0);
  }

  @Test
  public void getActiveSubscriptionInfoCountMax_shouldThrowExceptionWhenNoPermissions() {
    shadowOf(subscriptionManager).setReadPhoneStatePermission(false);
    assertThrows(
        SecurityException.class, () -> subscriptionManager.getActiveSubscriptionInfoCountMax());
  }

  @Test
  @Config(minSdk = O_MR1)
  public void getAccessibleSubscriptionInfoList() {
    SubscriptionInfo expectedSubscriptionInfo =
        SubscriptionInfoBuilder.newBuilder().setId(123).setIsEmbedded(true).buildSubscriptionInfo();

    // Default
    assertThat(shadowOf(subscriptionManager).getAccessibleSubscriptionInfoList()).isEmpty();

    // Null vararg
    shadowOf(subscriptionManager).setAccessibleSubscriptionInfos();
    assertThat(shadowOf(subscriptionManager).getAccessibleSubscriptionInfoList()).isEmpty();

    // A specific subscription
    shadowOf(subscriptionManager).setAccessibleSubscriptionInfos(expectedSubscriptionInfo);
    assertThat(shadowOf(subscriptionManager).getAccessibleSubscriptionInfoList())
        .containsExactly(expectedSubscriptionInfo);
  }

  @Test
  @Config(minSdk = O_MR1)
  public void setAccessibleSubscriptionInfoList_triggersSubscriptionsChanged() {
    DummySubscriptionsChangedListener listener = new DummySubscriptionsChangedListener();
    subscriptionManager.addOnSubscriptionsChangedListener(listener);
    // Invoked upon registration, but that's not important for this test
    int initialInvocationCount = listener.subscriptionChangedCount;

    shadowOf(subscriptionManager)
        .setAccessibleSubscriptionInfos(
            SubscriptionInfoBuilder.newBuilder()
                .setId(123)
                .setIsEmbedded(true)
                .buildSubscriptionInfo());

    assertThat(listener.subscriptionChangedCount - initialInvocationCount).isEqualTo(1);
  }

  @Test
  public void getAvailableSubscriptionInfoList() {
    SubscriptionInfo expectedSubscriptionInfo =
        SubscriptionInfoBuilder.newBuilder().setId(123).buildSubscriptionInfo();

    // default
    assertThat(shadowOf(subscriptionManager).getAvailableSubscriptionInfoList()).isEmpty();

    // null condition
    shadowOf(subscriptionManager).setAvailableSubscriptionInfos();
    assertThat(shadowOf(subscriptionManager).getAvailableSubscriptionInfoList()).isEmpty();

    // set a specific subscription
    shadowOf(subscriptionManager).setAvailableSubscriptionInfos(expectedSubscriptionInfo);
    assertThat(shadowOf(subscriptionManager).getAvailableSubscriptionInfoList()).hasSize(1);
    assertThat(shadowOf(subscriptionManager).getAvailableSubscriptionInfoList().get(0))
        .isSameInstanceAs(expectedSubscriptionInfo);
  }

  @Test
  public void getAllSubscriptionInfoList_default_isEmpty() {
    assertThat(shadowOf(subscriptionManager).getAllSubscriptionInfoList()).isEmpty();
  }

  @Test
  public void getAllSubscriptionInfoList_nullCondition_isEmpty() {
    shadowOf(subscriptionManager).setAllSubscriptionInfos();
    assertThat(shadowOf(subscriptionManager).getAllSubscriptionInfoList()).isEmpty();
  }

  @Test
  public void getAllSubscriptionInfoList_setSpecificSubscription_returnsList() {
    SubscriptionInfo expectedSubscriptionInfo =
        SubscriptionInfoBuilder.newBuilder().setId(123).buildSubscriptionInfo();
    shadowOf(subscriptionManager).setAllSubscriptionInfos(expectedSubscriptionInfo);
    assertThat(shadowOf(subscriptionManager).getAllSubscriptionInfoList()).hasSize(1);
    assertThat(shadowOf(subscriptionManager).getAllSubscriptionInfoList().get(0))
        .isSameInstanceAs(expectedSubscriptionInfo);
  }

  @Test
  public void setAvailableSubscriptionInfoList_triggersSubscriptionsChanged() {
    DummySubscriptionsChangedListener listener = new DummySubscriptionsChangedListener();
    subscriptionManager.addOnSubscriptionsChangedListener(listener);
    // Invoked upon registration, but that's not important for this test
    int initialInvocationCount = listener.subscriptionChangedCount;

    shadowOf(subscriptionManager)
        .setAvailableSubscriptionInfos(
            SubscriptionInfoBuilder.newBuilder().setId(123).buildSubscriptionInfo());

    assertThat(listener.subscriptionChangedCount - initialInvocationCount).isEqualTo(1);
  }

  @Test
  @Config(maxSdk = P)
  public void getPhoneId_shouldReturnPhoneIdIfSet() {
    ShadowSubscriptionManager.putPhoneId(123, 456);
    assertThat(SubscriptionManager.getPhoneId(123)).isEqualTo(456);
  }

  @Test
  @Config(maxSdk = P)
  public void getPhoneId_shouldReturnInvalidIfNotSet() {
    ShadowSubscriptionManager.putPhoneId(123, 456);
    assertThat(SubscriptionManager.getPhoneId(456))
        .isEqualTo(ShadowSubscriptionManager.INVALID_PHONE_INDEX);
  }

  @Test
  @Config(maxSdk = P)
  public void getPhoneId_shouldReturnInvalidIfRemoved() {
    ShadowSubscriptionManager.putPhoneId(123, 456);
    ShadowSubscriptionManager.removePhoneId(123);
    assertThat(SubscriptionManager.getPhoneId(123))
        .isEqualTo(ShadowSubscriptionManager.INVALID_PHONE_INDEX);
  }

  @Test
  @Config(maxSdk = P)
  public void getPhoneId_shouldReturnInvalidIfCleared() {
    ShadowSubscriptionManager.putPhoneId(123, 456);
    ShadowSubscriptionManager.clearPhoneIds();
    assertThat(SubscriptionManager.getPhoneId(123))
        .isEqualTo(ShadowSubscriptionManager.INVALID_PHONE_INDEX);
  }

  @Test
  @Config(maxSdk = P)
  public void getPhoneId_shouldReturnInvalidIfReset() {
    ShadowSubscriptionManager.putPhoneId(123, 456);
    ShadowSubscriptionManager.reset();
    assertThat(SubscriptionManager.getPhoneId(123))
        .isEqualTo(ShadowSubscriptionManager.INVALID_PHONE_INDEX);
  }

  @Test
  public void getSubId() {
    // Explicitly callable without any permissions.
    shadowOf(subscriptionManager).setReadPhoneStatePermission(false);

    assertThat(SubscriptionManager.getSubId(/* slotIndex= */ 0)).isNull();

    shadowOf(subscriptionManager)
        .setActiveSubscriptionInfos(
            SubscriptionInfoBuilder.newBuilder()
                .setId(123)
                .setSimSlotIndex(0)
                .buildSubscriptionInfo(),
            SubscriptionInfoBuilder.newBuilder()
                .setId(456)
                .setSimSlotIndex(1)
                .buildSubscriptionInfo());
    int[] subId = SubscriptionManager.getSubId(/* slotIndex= */ 0);
    assertThat(subId).hasLength(1);
    assertThat(subId[0]).isEqualTo(123);

    assertThat(SubscriptionManager.getSubId(/* slotIndex= */ 2)).isNull();
  }

  @Test
  @Config(minSdk = Q)
  public void getSubscriptionIds() {
    // Explicitly callable without any permissions.
    shadowOf(subscriptionManager).setReadPhoneStatePermission(false);

    assertThat(subscriptionManager.getSubscriptionIds(/* slotIndex= */ 0)).isNull();

    shadowOf(subscriptionManager)
        .setActiveSubscriptionInfos(
            SubscriptionInfoBuilder.newBuilder()
                .setId(123)
                .setSimSlotIndex(0)
                .buildSubscriptionInfo(),
            SubscriptionInfoBuilder.newBuilder()
                .setId(456)
                .setSimSlotIndex(1)
                .buildSubscriptionInfo());
    int[] subId = subscriptionManager.getSubscriptionIds(/* slotIndex= */ 0);
    assertThat(subId).hasLength(1);
    assertThat(subId[0]).isEqualTo(123);

    assertThat(subscriptionManager.getSubscriptionIds(/* slotIndex= */ 2)).isNull();
  }

  @Test
  @Config(minSdk = UPSIDE_DOWN_CAKE)
  public void getSubscriptionId() {
    // Explicitly callable without any permissions.
    shadowOf(subscriptionManager).setReadPhoneStatePermission(false);

    assertThat(SubscriptionManager.getSubscriptionId(/* slotIndex= */ 0))
        .isEqualTo(SubscriptionManager.INVALID_SUBSCRIPTION_ID);

    shadowOf(subscriptionManager)
        .setActiveSubscriptionInfos(
            SubscriptionInfoBuilder.newBuilder()
                .setId(123)
                .setSimSlotIndex(0)
                .buildSubscriptionInfo(),
            SubscriptionInfoBuilder.newBuilder()
                .setId(456)
                .setSimSlotIndex(1)
                .buildSubscriptionInfo());
    assertThat(SubscriptionManager.getSubscriptionId(/* slotIndex= */ 0)).isEqualTo(123);

    assertThat(SubscriptionManager.getSubscriptionId(/* slotIndex= */ 2))
        .isEqualTo(SubscriptionManager.INVALID_SUBSCRIPTION_ID);
  }

  @Test
  public void setMcc() {
    assertThat(
            ShadowSubscriptionManager.SubscriptionInfoBuilder.newBuilder()
                .setMcc("123")
                .buildSubscriptionInfo()
                .getMcc())
        .isEqualTo(123);
  }

  @Test
  public void setMnc() {
    assertThat(
            ShadowSubscriptionManager.SubscriptionInfoBuilder.newBuilder()
                .setMnc("123")
                .buildSubscriptionInfo()
                .getMnc())
        .isEqualTo(123);
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void getPhoneNumber_phoneNumberNotSet_returnsEmptyString() {
    assertThat(subscriptionManager.getPhoneNumber(SubscriptionManager.DEFAULT_SUBSCRIPTION_ID))
        .isEqualTo("");
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void getPhoneNumber_setPhoneNumber_returnsPhoneNumber() {
    shadowOf(subscriptionManager)
        .setPhoneNumber(SubscriptionManager.DEFAULT_SUBSCRIPTION_ID, "123");
    assertThat(subscriptionManager.getPhoneNumber(SubscriptionManager.DEFAULT_SUBSCRIPTION_ID))
        .isEqualTo("123");
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void getPhoneNumber_shouldThrowExceptionWhenNoPermissions() {
    shadowOf(subscriptionManager).setReadPhoneNumbersPermission(false);
    assertThrows(
        SecurityException.class,
        () ->
            shadowOf(subscriptionManager)
                .getPhoneNumber(SubscriptionManager.DEFAULT_SUBSCRIPTION_ID));
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void getPhoneNumberWithSource_phoneNumberNotSet_returnsEmptyString() {
    assertThat(
            subscriptionManager.getPhoneNumber(
                SubscriptionManager.DEFAULT_SUBSCRIPTION_ID,
                SubscriptionManager.PHONE_NUMBER_SOURCE_UICC))
        .isEqualTo("");
    assertThat(
            subscriptionManager.getPhoneNumber(
                SubscriptionManager.DEFAULT_SUBSCRIPTION_ID,
                SubscriptionManager.PHONE_NUMBER_SOURCE_CARRIER))
        .isEqualTo("");
    assertThat(
            subscriptionManager.getPhoneNumber(
                SubscriptionManager.DEFAULT_SUBSCRIPTION_ID,
                SubscriptionManager.PHONE_NUMBER_SOURCE_IMS))
        .isEqualTo("");
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void getPhoneNumberWithSource_setPhoneNumber_returnsPhoneNumber() {
    shadowOf(subscriptionManager)
        .setPhoneNumber(SubscriptionManager.DEFAULT_SUBSCRIPTION_ID, "123");
    assertThat(
            subscriptionManager.getPhoneNumber(
                SubscriptionManager.DEFAULT_SUBSCRIPTION_ID,
                SubscriptionManager.PHONE_NUMBER_SOURCE_UICC))
        .isEqualTo("123");
    assertThat(
            subscriptionManager.getPhoneNumber(
                SubscriptionManager.DEFAULT_SUBSCRIPTION_ID,
                SubscriptionManager.PHONE_NUMBER_SOURCE_CARRIER))
        .isEqualTo("123");
    assertThat(
            subscriptionManager.getPhoneNumber(
                SubscriptionManager.DEFAULT_SUBSCRIPTION_ID,
                SubscriptionManager.PHONE_NUMBER_SOURCE_IMS))
        .isEqualTo("123");
  }

  @Test
  @Config(minSdk = Q)
  public void setIsOpportunistic_shouldReturnFalse() {
    assertThat(
            ShadowSubscriptionManager.SubscriptionInfoBuilder.newBuilder()
                .setIsOpportunistic(false)
                .buildSubscriptionInfo()
                .isOpportunistic())
        .isFalse();
  }

  @Test
  @Config(minSdk = Q)
  public void setIsOpportunistic_shouldReturnTrue() {
    assertThat(
            ShadowSubscriptionManager.SubscriptionInfoBuilder.newBuilder()
                .setIsOpportunistic(true)
                .buildSubscriptionInfo()
                .isOpportunistic())
        .isTrue();
  }

  @Test
  @Config(minSdk = O)
  public void getSlotIndex_nullSubscriptionList_returnsInvalidSlotIndex() {
    assertThat(SubscriptionManager.getSlotIndex(/* subscriptionId= */ 2))
        .isEqualTo(INVALID_SIM_SLOT_INDEX);
  }

  @Test
  @Config(minSdk = O)
  public void getSlotIndex_unknownSubscriptionId_returnsInvalidSlotIndex() {
    new ShadowSubscriptionManager()
        .setActiveSubscriptionInfos(
            SubscriptionInfoBuilder.newBuilder()
                .setId(2)
                .setSimSlotIndex(0)
                .buildSubscriptionInfo());

    assertThat(SubscriptionManager.getSlotIndex(/* subscriptionId= */ 3))
        .isEqualTo(INVALID_SIM_SLOT_INDEX);
  }

  @Test
  @Config(minSdk = O)
  public void getSlotIndex_knownSubscriptionId_returnsMatchingSlotIndex() {
    new ShadowSubscriptionManager()
        .setActiveSubscriptionInfos(
            SubscriptionInfoBuilder.newBuilder()
                .setId(2)
                .setSimSlotIndex(1)
                .buildSubscriptionInfo());

    assertThat(SubscriptionManager.getSlotIndex(/* subscriptionId= */ 2)).isEqualTo(1);
  }

  private static class DummySubscriptionsChangedListener
      extends SubscriptionManager.OnSubscriptionsChangedListener {
    private int subscriptionChangedCount;

    @Override
    public void onSubscriptionsChanged() {
      subscriptionChangedCount++;
    }
  }

  @Test
  @Config(minSdk = O)
  public void
      subscriptionManager_activityContextEnabled_differentInstancesRetrieveDefaultSubscriptionInfo() {
    setSystemPropertyRule.set("robolectric.createActivityContexts", "true");

    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      SubscriptionManager applicationSubscriptionManager =
          (SubscriptionManager)
              RuntimeEnvironment.getApplication()
                  .getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
      Activity activity = controller.get();
      SubscriptionManager activitySubscriptionManager =
          (SubscriptionManager) activity.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

      assertThat(applicationSubscriptionManager).isNotSameInstanceAs(activitySubscriptionManager);

      int defaultSubscriptionId = SubscriptionManager.getDefaultSubscriptionId();
      SubscriptionInfo applicationDefaultSubscriptionInfo =
          applicationSubscriptionManager.getActiveSubscriptionInfo(defaultSubscriptionId);
      SubscriptionInfo activityDefaultSubscriptionInfo =
          activitySubscriptionManager.getActiveSubscriptionInfo(defaultSubscriptionId);

      assertThat(applicationDefaultSubscriptionInfo).isEqualTo(activityDefaultSubscriptionInfo);
    }
  }
}

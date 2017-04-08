package com.terrestrial.pelconner;

import android.content.ComponentName;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.ActivityTestRule;

import junit.framework.TestCase;

import org.junit.Ignore;
import org.junit.Rule;

import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.toPackage;

/**
 * Created by vladast on 3.4.17..
 */

@Ignore
public class PelconnerTest extends TestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public static class Pelconner {

        @Rule
        public static IntentsTestRule<PelconnerSplashActivity> mActivityRule =
                new IntentsTestRule<>(PelconnerSplashActivity.class, true);

        static void start() {
            Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), PelconnerSplashActivity.class);
            mActivityRule.launchActivity(intent);
        }

        public static class Splash {

            public static void verify() {
                //intended(hasComponent(PelconnerSplashActivity.class.getName()));
                //intended(hasComponent(new ComponentName(InstrumentationRegistry.getTargetContext(), PelconnerSplashActivity.class)));
                intended(toPackage(PelconnerMainActivity.class.getName() + ".vladast"));
            }
        }
    }
}

package com.terrestrial.pelconner;


import android.content.ComponentName;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class PelconnerSplashActivityTest extends PelconnerTest {


//    @Rule
//    public TestRule watcher = new TestWatcher() {
//        @Override
//        protected void failed(Throwable e, Description description) {
//            // Save to external storage (usually /sdcard/screenshots)
//            File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
//                    + "/screenshots/" + getTargetContext().getPackageName());
//            if (!path.exists()) {
//                path.mkdirs();
//            }
//
//            // Take advantage of UiAutomator screenshot method
//            UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
//            String filename = description.getClassName() + "-" + description.getMethodName() + ".png";
//            device.takeScreenshot(new File(path, filename));
//        }
//    };

/*    @Rule
    public TestRule watcher = new TestRule() {
        @Override
        public Statement apply(Statement statement, org.junit.runner.Description description) {

            return new Statement();
        }
    }*/

    @Test
    public void pelconnerSplashActivityTest() {

        Pelconner.start();

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Pelconner.Splash.verify();

        //intended(hasComponent(YourExpectedActivity.class.getName()));
        //intended(hasComponent(new ComponentName(InstrumentationRegistry.getTargetContext(), PelconnerSplashActivity.class)));


        ViewInteraction buttonNewPicture = onView(withId(R.id.buttonNewPicture));
        buttonNewPicture.check(matches(isDisplayed()));
        buttonNewPicture.perform(click());

        ViewInteraction button = onView(
                allOf(withId(R.id.buttonNewPicture),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.FrameLayout")),
                                        2),
                                0),
                        isDisplayed()));
        button.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction textView = onView(
                allOf(withId(android.R.id.title), withText("Tools"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("com.android.internal.view.menu.ListMenuItemView")),
                                        0),
                                0),
                        isDisplayed()));
        textView.perform(click());

        DataInteraction linearLayout = onData(anything())
                .inAdapterView(allOf(withId(R.id.listViewOptions),
                        childAtPosition(
                                withClassName(is("android.widget.RelativeLayout")),
                                1)))
                .atPosition(4);
        linearLayout.perform(click());

        ViewInteraction imageView = onView(
                allOf(withId(R.id.imageViewPicturePreview),
                        childAtPosition(
                                allOf(withId(R.id.relativeLayoutContainer),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                0),
                        isDisplayed()));
        imageView.check(matches(isDisplayed()));

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}

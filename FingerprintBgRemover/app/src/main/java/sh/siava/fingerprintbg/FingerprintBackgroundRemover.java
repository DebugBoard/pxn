package sh.siava.fingerprintbg;

import android.view.View;
import android.widget.ImageView;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * Hides the background circle that sits behind the lock screen fingerprint (device entry) icon,
 * leaving the icon itself untouched.
 * <p>
 * Android 14+ draws it as the {@code bgView} child of {@code DeviceEntryIconView}, Android 12/13
 * as the {@code mBgView} child of {@code LockIconView}. Both are plain {@link ImageView}s, so
 * dropping their image alpha to zero removes the background without touching the layout - which
 * keeps the touch target, the UDFPS sensor area and the icon animations exactly where they were.
 */
public class FingerprintBackgroundRemover implements IXposedHookLoadPackage {
	private static final String TAG = "FingerprintBgRemover";
	private static final String SYSTEM_UI = "com.android.systemui";
	private static final String ALREADY_HANDLED = "fpBgRemoverHandled";
	private static final int TRANSPARENT = 0;

	@Override
	public void handleLoadPackage(LoadPackageParam lpParam) {
		if (!SYSTEM_UI.equals(lpParam.packageName)) return;

		//Android 14+
		hideBackgroundOf(lpParam.classLoader,
				"com.android.systemui.keyguard.ui.view.DeviceEntryIconView", "bgView");

		//Android 12/13
		hideBackgroundOf(lpParam.classLoader,
				"com.android.systemui.statusbar.phone.LockIconView", "mBgView");
	}

	private void hideBackgroundOf(ClassLoader classLoader, String className, String fieldName) {
		Class<?> viewClass = XposedHelpers.findClassIfExists(className, classLoader);
		if (viewClass == null) return;

		XC_MethodHook hook = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) {
				hideBackground(param.thisObject, fieldName);
			}
		};

		//the background is created in the constructor on A14+, and inflated on A12/13
		XposedBridge.hookAllConstructors(viewClass, hook);
		try {
			XposedHelpers.findAndHookMethod(viewClass, "onFinishInflate", hook);
		} catch (Throwable ignored) {
		}
	}

	private void hideBackground(Object iconView, String fieldName) {
		Object background;
		try {
			background = XposedHelpers.getObjectField(iconView, fieldName);
		} catch (Throwable t) {
			XposedBridge.log(TAG + ": " + fieldName + " not found - " + t);
			return;
		}

		if (!(background instanceof ImageView backgroundView)) return;
		if (XposedHelpers.getAdditionalInstanceField(backgroundView, ALREADY_HANDLED) != null) return;
		XposedHelpers.setAdditionalInstanceField(backgroundView, ALREADY_HANDLED, true);

		backgroundView.setImageAlpha(TRANSPARENT);

		//SystemUI re-binds the background whenever the icon state changes, so keep it transparent.
		//setImageAlpha() only invalidates, it never requests another layout pass.
		backgroundView.addOnLayoutChangeListener(
				(v, l, t, r, b, oldL, oldT, oldR, oldB) -> ((ImageView) v).setImageAlpha(TRANSPARENT));
	}
}

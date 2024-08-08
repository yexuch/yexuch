package lin.xposed.hook.item;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import lin.xposed.hook.HookItem;
import lin.xposed.hook.load.base.BaseSwitchFunctionHookItem;
import lin.xposed.hook.util.qq.ToastTool;

@HookItem("净化/侧滑栏/精简侧滑栏右上角二维码")
public class QQSettingMeViewQRCode extends BaseSwitchFunctionHookItem {
    @Override
    public void loadHook(ClassLoader loader) throws Exception {
        Class<?> settingView = loader.loadClass("com.tencent.mobileqq.activity.QQSettingMe");
        Class<?> resultClass = loader.loadClass("com.tencent.mobileqq.activity.BaseQQSettingMeView");
        Loop : for (Class<?> currentClass = settingView; currentClass != null; currentClass = currentClass.getSuperclass()) {
            for (Method method : currentClass.getDeclaredMethods()) {
                method.setAccessible(true);
                if (method.getReturnType() == resultClass) {
                    XposedBridge.hookMethod(method, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            //获取返回对象
                            Object BaseSettingMeView = param.getResult();
                            //获取返回对象的父类
                            Class<?> au = BaseSettingMeView.getClass().getSuperclass().getSuperclass();

                            Field viewGroupField = null;
                            for (Field field : au.getDeclaredFields()) {
                                field.setAccessible(true);
                                //判断类型符合ViewGroup
                                if (field.getType() == ViewGroup.class) {
                                    viewGroupField = field;
                                    break;
                                }
                            }
                            ViewGroup viewGroup = (ViewGroup) viewGroupField.get(BaseSettingMeView);

                            //三角形具有稳定性
                            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                                View view = viewGroup.getChildAt(i);
                                if (view instanceof FrameLayout QRCodeLayout) {
                                    if (QRCodeLayout.getChildCount() == 1) {
                                        if (QRCodeLayout.getChildAt(0) instanceof FrameLayout frameLayout) {
                                            if (frameLayout.getChildCount() == 2) {
                                                if (frameLayout.getChildAt(0) instanceof ImageView) {
                                                    viewGroup.removeView(QRCodeLayout);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }

                            }
                        }
                    });
                    break Loop;
                }
            }
        }
    }

    @Override
    public String getTips() {
        return "重启生效,该项仅在旧版侧滑栏生效";
    }
}

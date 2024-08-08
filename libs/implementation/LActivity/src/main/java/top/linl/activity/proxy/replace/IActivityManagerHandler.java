package top.linl.activity.proxy.replace;

import android.content.Intent;

import androidx.core.util.Pair;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import top.linl.activity.proxy.ActivityProxyManager;
import top.linl.activity.proxy.ActivityProxyEnvInfo;

public class IActivityManagerHandler implements InvocationHandler {
    private final Object activityManager;

    public IActivityManagerHandler(Object activityManager) {
        this.activityManager = activityManager;
    }

    private Pair<Integer, Intent> foundFirstIntentOfArgs(Object[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Intent) {
                return new Pair<>(i, (Intent) args[i]);
            }
        }
        return null;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            if (args != null) {
                if (method.getName().equals("startActivity")) {
                    Pair<Integer, Intent> pair = foundFirstIntentOfArgs(args);
                    if (pair != null) {
                        Intent intent = pair.second;
                        if (intent.getComponent() != null) {
                            String packageName = intent.getComponent().getPackageName();
                            String className = intent.getComponent().getClassName();
                            if (packageName.equals(ActivityProxyEnvInfo.HostContext.getPackageName())
                                    && ActivityProxyManager.isModuleActivity(className)) {
                                Intent wrapper = new Intent();
                                wrapper.setClassName(
                                        intent.getComponent().getPackageName(),
                                        ActivityProxyManager.HostActivityClassName
                                );
                                wrapper.putExtra(
                                        ActivityProxyManager.ACTIVITY_PROXY_INTENT,
                                        pair.second
                                );
                                args[pair.first] = wrapper;
                            }
                        }
                    }
                }
                return method.invoke(activityManager, args);
            }
            return method.invoke(activityManager);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
}

package com.taobao.luaview.global;

import android.content.Context;
import android.text.TextUtils;

import com.taobao.luaview.scriptbundle.LuaScriptManager;
import com.taobao.luaview.scriptbundle.ScriptBundle;
import com.taobao.luaview.scriptbundle.asynctask.ScriptBundleDownloadTask;
import com.taobao.luaview.scriptbundle.asynctask.ScriptBundleLoadTask;
import com.taobao.luaview.scriptbundle.asynctask.ScriptBundleUnpackTask;
import com.taobao.luaview.scriptbundle.asynctask.SimpleTask1;
import com.taobao.luaview.util.FileUtil;

import org.luaj.vm2.LuaValue;

/**
 * script loader
 *
 * @author song
 * @date 15/11/10
 */
public class LuaScriptLoader {
    private Context mContext;

    public LuaScriptLoader(final Context context) {
        if (context != null) {
            this.mContext = context.getApplicationContext();
        }
        LuaScriptManager.init(context);
    }

    public interface ScriptLoaderCallback {
        void onScriptLoaded(final ScriptBundle bundle);//脚本加载
    }

    /**
     * 脚本运行回调
     */
    public interface ScriptExecuteCallback {
        /**
         * 脚本准备完毕, 返回true，表示需要自己处理执行，onScriptCompiled，onScriptExecuted不会被执行，返回false表示系统继续执行
         *
         * @param bundle
         * @return
         */
        boolean onScriptPrepared(ScriptBundle bundle);

        /**
         * 脚本编译完成，参数表示编译之后的结果，不保证一定被调用到
         * 返回true，表示需要自己处理执行，返回false，表示系统继续执行
         *
         * @param value
         */
        boolean onScriptCompiled(LuaValue value, LuaValue context, LuaValue view);

        /**
         * 脚本执行完成，参数表示是否执行成功，保证一定被调用到
         * @param uri 原始的加载url
         * @param executedSuccess
         */
        void onScriptExecuted(String uri, boolean executedSuccess);
    }

    //-----------------------------------------static methods---------------------------------------

    /**
     * 将asset目录下所有bundle包解压缩出来，一般在初始化的时候调用
     *
     * @param basePath
     */
    public void unpackAllAssetBundle(final String basePath) {
        if (basePath != null) {
            ScriptBundleUnpackTask.unpackAllAssetScripts(mContext, basePath);
        }
    }

    /**
     * clear invalid bundle of given url
     * TODO 在脚本加载失败的时候调用清理函数
     *
     * @param url
     */
    public static void clearInvalidBundle(final String url) {
        if (!TextUtils.isEmpty(url)) {
            new SimpleTask1<Void>() {
                @Override
                protected Void doInBackground(Object... params) {
                    String folderPath = LuaScriptManager.buildScriptBundleFolderPath(url);
                    FileUtil.delete(folderPath);
                    return null;
                }
            }.execute();
        }
    }

    //--------------------------------lua script get and decode-------------------------------------

    /**
     * fetch a script from network (if needed) or from local file system
     *
     * @param url
     * @param callback
     */
    public void load(final String url, final ScriptLoaderCallback callback) {
        load(url, null, callback);
    }

    /**
     * fetch a script from network (if needed) or from local file system
     *
     * @param url
     * @param sha256
     * @param callback
     */
    public void load(final String url, final String sha256, final ScriptLoaderCallback callback) {
        if (LuaScriptManager.existsScriptBundle(url)) {//load local
            new ScriptBundleLoadTask(mContext, callback).execute(url);
        } else {
            new ScriptBundleDownloadTask(mContext, callback).execute(url, sha256);
        }
    }

}

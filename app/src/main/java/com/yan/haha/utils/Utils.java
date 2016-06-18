package com.yan.haha.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.graphics.Palette;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.yan.haha.MainApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

public class Utils {

    public static final String FILE_TYPE_IMAGE = "image/*";

    public static final int SIZE_NOT_CHANGE = -255;

    public static int getScreenHeight() {
        Context context = MainApplication.getContext();
        if (context != null) {
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            return dm.heightPixels;
        } else {
            return -1;
        }
    }

    public static int getScreenWidth() {
        Context context = MainApplication.getContext();
        if (context != null) {
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            return dm.widthPixels;
        } else {
            return -1;
        }
    }

    public static int getResourceIdByName(String name, String type) {
        Context context = MainApplication.getContext();
        if (context != null) {
            return context.getResources().getIdentifier(name, type, context.getPackageName());
        }
        return -1;
    }

    public static int getRandomNumber(int min, int max) {
        int num = new Random().nextInt(max - min + 1) + min;
        if (num > max) {
            num = max;
        }
        return num;
    }

    public static boolean canScroll(ScrollView scrollView) {
        View child = scrollView.getChildAt(0);
        if (child != null) {
            int childHeight = child.getHeight();
            return scrollView.getHeight() <
                    childHeight + scrollView.getPaddingTop() + scrollView.getPaddingBottom();
        }
        return false;
    }

    public static boolean isScreenLandscape() {
        Context ctx = MainApplication.getContext();
        if (ctx != null) {
            Configuration cfg = ctx.getResources().getConfiguration();
            return (cfg.orientation == Configuration.ORIENTATION_LANDSCAPE);
        } else {
            return false;
        }
    }

    public static abstract class BitmapColorCallback {
        public void onGenerated(Palette palette) {}
    }

    public static void getBitmapColor(Bitmap bmp, final BitmapColorCallback callback) {
        Palette.from(bmp).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                if (callback != null) {
                    callback.onGenerated(palette);
                }
            }
        });
    }

    public static void scanFileForUpdate(File file) {
        MediaScannerConnection.scanFile(
                MainApplication.getContext(),
                new String[]{ file.getPath() },
                null, null);
    }

    /**
     * 将 View 的显示转成 Bitmap
     */
    public static Bitmap convertViewToBitmap(View view) {
        Bitmap b = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        view.draw(c);
        return b;
    }

    /**
     * 将 view 转成图片并保存下来
     * @param view 要保存为图片的控件
     * @param path 保存路径
     * @return boolean 是否保存成功
     */
    public static boolean saveViewAsPicture(View view, String path) {
        Bitmap bitmap = convertViewToBitmap(view);
        if (bitmap != null) {
            try {
                FileOutputStream fos = new FileOutputStream(path);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

                // 通知各应用刷新文件
                scanFileForUpdate(new File(path));
                Log.i("yan", "saveViewAsPicture sucessfully");
                return true;
            } catch (Exception e) {
                Log.i("yan", "saveViewAsPicture error: " + e.getMessage());
            }
        } else {
            Log.i("yan", "saveViewAsPicture error: bitmap null");
        }
        return false;
    }

    /**
     * 异步将 view 转成图片并保存下来
     * @param view 要保存为图片的控件
     * @param path 保存路径
     * @param success 保存成功回调函数
     * @param fail 保存失败回调函数
     */
    public static void saveViewAsPicture(final View view, final String path,
                                         final Runnable success, final Runnable fail) {
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                if (saveViewAsPicture(view, path)) {
                    return true;
                }
                return false;
            }

            @Override
            protected void onPostExecute(Object obj) {
                boolean ret = (boolean) obj;
                if (ret) {
                    if (success != null) {
                        success.run();
                    }
                } else if (fail != null) {
                    fail.run();
                }
            }
        };
        task.execute();
    }

    public static void share(Context context, String text) {
        if (context != null) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, text);
            sendIntent.setType("text/plain");
            context.startActivity(sendIntent);
        }
    }

    public static void share(File file, String fileType) {
        Context ctx = MainApplication.getContext();
        if (ctx != null && file.exists() && file.isFile()) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            sendIntent.setType(fileType);
            sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(sendIntent);
        }
    }

    public static void setViewSize(View view, int width, int height) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (width != SIZE_NOT_CHANGE) {
            lp.width = width;
        }
        if (height != SIZE_NOT_CHANGE) {
            lp.height = height;
        }
        view.setLayoutParams(lp);
    }

    public static void setViewMargin(View view, int t, int r, int d, int l) {
        ViewGroup.MarginLayoutParams mp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        if (t != SIZE_NOT_CHANGE) {
            mp.topMargin = t;
        }
        if (r != SIZE_NOT_CHANGE) {
            mp.rightMargin = r;
        }
        if (d != SIZE_NOT_CHANGE) {
            mp.bottomMargin = d;
        }
        if (l != SIZE_NOT_CHANGE) {
            mp.leftMargin = l;
        }
        view.setLayoutParams(mp);
    }

    public static int getDimen(int resId) {
        Context ctx = MainApplication.getContext();
        if (ctx != null) {
            return ctx.getResources().getDimensionPixelSize(resId);
        } else {
            return -1;
        }
    }
}

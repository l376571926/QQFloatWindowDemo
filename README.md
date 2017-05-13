# QQFloatWindowDemo
Android桌面悬浮窗进阶，QQ手机管家小火箭效果实现http://blog.csdn.net/guolin_blog/article/details/16919859（我只是个搬运工）

Android桌面悬浮窗进阶，QQ手机管家小火箭效果实现

版权声明：本文出自郭霖的博客，转载必须注明出处。
转载请注明出处：http://blog.csdn.net/guolin_blog/article/details/16919859

今天是2013年的最后一天了，这里首先提前祝大家新年快乐！同时，本篇文章也是我今年的最后一篇文章了，因此我想要让它尽量有点特殊性，比起平时的文章要多一些特色。记得在今年年初的时候，我写的第一篇文章是模仿360手机卫士的桌面悬浮窗效果，那么为了能够首尾呼应，今年的最后一篇文章就同样还是来实现桌面悬浮窗的效果吧，当然效果将会更加高级。

相信用过QQ手机管家的朋友们都会知道它有一个小火箭加速的功能，将小火箭拖动到火箭发射台上发射就会出现一个火箭升空的动画，那么今天我们就来模仿着实现一下这个效果吧。

这次我们将代码的重点放在火箭升空的效果上，因此简单起见，就直接在模仿360手机卫士悬浮窗的那份代码的基础上继续开发了，如果你还没有看过那篇文章的话，建议先去阅读 Android桌面悬浮窗效果实现，仿360手机卫士悬浮窗效果 。

比起普通的桌面悬浮窗，现在我们需要在拖动悬浮窗的时候将悬浮窗变成一个小火箭，并且在屏幕的底部添加一个火箭发射台。那么我们就从火箭发射台开始编写吧，首先创建launcher.xml作为火箭发射台的布局文件，如下所示：
[html] view plain copy 在CODE上查看代码片派生到我的代码片
<?xml version="1.0" encoding="UTF-8"?>  
<LinearLayout  
    xmlns:android="http://schemas.android.com/apk/res/android"  
    android:layout_width="wrap_content"  
    android:layout_height="wrap_content"  
    android:orientation="vertical"  
    >  
      
    <ImageView   
        android:id="@+id/launcher_img"  
        android:layout_width="200dp"  
        android:layout_height="88dp"  
        android:src="@drawable/launcher_bg_hold"  
        />  
      
</LinearLayout>  
可以看到，这里的ImageView是用于显示当前火箭发射台状态的。我事先准备好了两张图片，一张是当小火箭未拖动到火箭发射台时显示的，一张是当小火箭拖动到火箭发射台上时显示的。

接下来创建RocketLauncher类，作为火箭发射台的View，代码如下所示：
[java] view plain copy 在CODE上查看代码片派生到我的代码片
public class RocketLauncher extends LinearLayout {  
  
    /** 
     * 记录火箭发射台的宽度 
     */  
    public static int width;  
  
    /** 
     * 记录火箭发射台的高度 
     */  
    public static int height;  
  
    /** 
     * 火箭发射台的背景图片 
     */  
    private ImageView launcherImg;  
  
    public RocketLauncher(Context context) {  
        super(context);  
        LayoutInflater.from(context).inflate(R.layout.launcher, this);  
        launcherImg = (ImageView) findViewById(R.id.launcher_img);  
        width = launcherImg.getLayoutParams().width;  
        height = launcherImg.getLayoutParams().height;  
    }  
  
    /** 
     * 更新火箭发射台的显示状态。如果小火箭被拖到火箭发射台上，就显示发射。 
     */  
    public void updateLauncherStatus(boolean isReadyToLaunch) {  
        if (isReadyToLaunch) {  
            launcherImg.setImageResource(R.drawable.launcher_bg_fire);  
        } else {  
            launcherImg.setImageResource(R.drawable.launcher_bg_hold);  
        }  
    }  
  
}  
RocketLauncher中的代码还是非常简单的，在构建方法中调用了LayoutInflater的inflate()方法来将launcher.xml这个布局文件加载进来，并获取到了当前View的宽度和高度。在updateLauncherStatus()方法中会进行判断，如果传入的参数是true，就显示小火箭即将发射的图片，如果传入的是false，就显示将小火箭拖动到发射台的图片。

新增的文件只有这两个，剩下的就是要修改之前的代码了。首先修改MyWindowManager中的代码，如下所示：
[java] view plain copy 在CODE上查看代码片派生到我的代码片
public class MyWindowManager {  
  
    /** 
     * 小悬浮窗View的实例 
     */  
    private static FloatWindowSmallView smallWindow;  
  
    /** 
     * 大悬浮窗View的实例 
     */  
    private static FloatWindowBigView bigWindow;  
  
    /** 
     * 火箭发射台的实例 
     */  
    private static RocketLauncher rocketLauncher;  
  
    /** 
     * 小悬浮窗View的参数 
     */  
    private static LayoutParams smallWindowParams;  
  
    /** 
     * 大悬浮窗View的参数 
     */  
    private static LayoutParams bigWindowParams;  
  
    /** 
     * 火箭发射台的参数 
     */  
    private static LayoutParams launcherParams;  
  
    /** 
     * 用于控制在屏幕上添加或移除悬浮窗 
     */  
    private static WindowManager mWindowManager;  
  
    /** 
     * 用于获取手机可用内存 
     */  
    private static ActivityManager mActivityManager;  
  
    /** 
     * 创建一个小悬浮窗。初始位置为屏幕的右部中间位置。 
     */  
    public static void createSmallWindow(Context context) {  
        WindowManager windowManager = getWindowManager(context);  
        int screenWidth = windowManager.getDefaultDisplay().getWidth();  
        int screenHeight = windowManager.getDefaultDisplay().getHeight();  
        if (smallWindow == null) {  
            smallWindow = new FloatWindowSmallView(context);  
            if (smallWindowParams == null) {  
                smallWindowParams = new LayoutParams();  
                smallWindowParams.type = LayoutParams.TYPE_SYSTEM_ALERT;  
                smallWindowParams.format = PixelFormat.RGBA_8888;  
                smallWindowParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL  
                        | LayoutParams.FLAG_NOT_FOCUSABLE;  
                smallWindowParams.gravity = Gravity.LEFT | Gravity.TOP;  
                smallWindowParams.width = FloatWindowSmallView.windowViewWidth;  
                smallWindowParams.height = FloatWindowSmallView.windowViewHeight;  
                smallWindowParams.x = screenWidth;  
                smallWindowParams.y = screenHeight / 2;  
            }  
            smallWindow.setParams(smallWindowParams);  
            windowManager.addView(smallWindow, smallWindowParams);  
        }  
    }  
  
    /** 
     * 将小悬浮窗从屏幕上移除。 
     */  
    public static void removeSmallWindow(Context context) {  
        if (smallWindow != null) {  
            WindowManager windowManager = getWindowManager(context);  
            windowManager.removeView(smallWindow);  
            smallWindow = null;  
        }  
    }  
  
    /** 
     * 创建一个大悬浮窗。位置为屏幕正中间。 
     */  
    public static void createBigWindow(Context context) {  
        WindowManager windowManager = getWindowManager(context);  
        int screenWidth = windowManager.getDefaultDisplay().getWidth();  
        int screenHeight = windowManager.getDefaultDisplay().getHeight();  
        if (bigWindow == null) {  
            bigWindow = new FloatWindowBigView(context);  
            if (bigWindowParams == null) {  
                bigWindowParams = new LayoutParams();  
                bigWindowParams.x = screenWidth / 2  
                        - FloatWindowBigView.viewWidth / 2;  
                bigWindowParams.y = screenHeight / 2  
                        - FloatWindowBigView.viewHeight / 2;  
                bigWindowParams.type = LayoutParams.TYPE_PHONE;  
                bigWindowParams.format = PixelFormat.RGBA_8888;  
                bigWindowParams.gravity = Gravity.LEFT | Gravity.TOP;  
                bigWindowParams.width = FloatWindowBigView.viewWidth;  
                bigWindowParams.height = FloatWindowBigView.viewHeight;  
            }  
            windowManager.addView(bigWindow, bigWindowParams);  
        }  
    }  
  
    /** 
     * 将大悬浮窗从屏幕上移除。 
     */  
    public static void removeBigWindow(Context context) {  
        if (bigWindow != null) {  
            WindowManager windowManager = getWindowManager(context);  
            windowManager.removeView(bigWindow);  
            bigWindow = null;  
        }  
    }  
  
    /** 
     * 创建一个火箭发射台，位置为屏幕底部。 
     */  
    public static void createLauncher(Context context) {  
        WindowManager windowManager = getWindowManager(context);  
        int screenWidth = windowManager.getDefaultDisplay().getWidth();  
        int screenHeight = windowManager.getDefaultDisplay().getHeight();  
        if (rocketLauncher == null) {  
            rocketLauncher = new RocketLauncher(context);  
            if (launcherParams == null) {  
                launcherParams = new LayoutParams();  
                launcherParams.x = screenWidth / 2 - RocketLauncher.width / 2;  
                launcherParams.y = screenHeight - RocketLauncher.height;  
                launcherParams.type = LayoutParams.TYPE_PHONE;  
                launcherParams.format = PixelFormat.RGBA_8888;  
                launcherParams.gravity = Gravity.LEFT | Gravity.TOP;  
                launcherParams.width = RocketLauncher.width;  
                launcherParams.height = RocketLauncher.height;  
            }  
            windowManager.addView(rocketLauncher, launcherParams);  
        }  
    }  
  
    /** 
     * 将火箭发射台从屏幕上移除。 
     */  
    public static void removeLauncher(Context context) {  
        if (rocketLauncher != null) {  
            WindowManager windowManager = getWindowManager(context);  
            windowManager.removeView(rocketLauncher);  
            rocketLauncher = null;  
        }  
    }  
  
    /** 
     * 更新火箭发射台的显示状态。 
     */  
    public static void updateLauncher() {  
        if (rocketLauncher != null) {  
            rocketLauncher.updateLauncherStatus(isReadyToLaunch());  
        }  
    }  
  
    /** 
     * 更新小悬浮窗的TextView上的数据，显示内存使用的百分比。 
     *  
     * @param context 
     *            可传入应用程序上下文。 
     */  
    public static void updateUsedPercent(Context context) {  
        if (smallWindow != null) {  
            TextView percentView = (TextView) smallWindow  
                    .findViewById(R.id.percent);  
            percentView.setText(getUsedPercentValue(context));  
        }  
    }  
  
    /** 
     * 是否有悬浮窗(包括小悬浮窗和大悬浮窗)显示在屏幕上。 
     *  
     * @return 有悬浮窗显示在桌面上返回true，没有的话返回false。 
     */  
    public static boolean isWindowShowing() {  
        return smallWindow != null || bigWindow != null;  
    }  
  
    /** 
     * 判断小火箭是否准备好发射了。 
     *  
     * @return 当火箭被发到发射台上返回true，否则返回false。 
     */  
    public static boolean isReadyToLaunch() {  
        if ((smallWindowParams.x > launcherParams.x && smallWindowParams.x  
                + smallWindowParams.width < launcherParams.x  
                + launcherParams.width)  
                && (smallWindowParams.y + smallWindowParams.height > launcherParams.y)) {  
            return true;  
        }  
        return false;  
    }  
  
    /** 
     * 如果WindowManager还未创建，则创建一个新的WindowManager返回。否则返回当前已创建的WindowManager。 
     *  
     * @param context 
     *            必须为应用程序的Context. 
     * @return WindowManager的实例，用于控制在屏幕上添加或移除悬浮窗。 
     */  
    private static WindowManager getWindowManager(Context context) {  
        if (mWindowManager == null) {  
            mWindowManager = (WindowManager) context  
                    .getSystemService(Context.WINDOW_SERVICE);  
        }  
        return mWindowManager;  
    }  
  
    /** 
     * 如果ActivityManager还未创建，则创建一个新的ActivityManager返回。否则返回当前已创建的ActivityManager。 
     *  
     * @param context 
     *            可传入应用程序上下文。 
     * @return ActivityManager的实例，用于获取手机可用内存。 
     */  
    private static ActivityManager getActivityManager(Context context) {  
        if (mActivityManager == null) {  
            mActivityManager = (ActivityManager) context  
                    .getSystemService(Context.ACTIVITY_SERVICE);  
        }  
        return mActivityManager;  
    }  
  
    /** 
     * 计算已使用内存的百分比，并返回。 
     *  
     * @param context 
     *            可传入应用程序上下文。 
     * @return 已使用内存的百分比，以字符串形式返回。 
     */  
    public static String getUsedPercentValue(Context context) {  
        String dir = "/proc/meminfo";  
        try {  
            FileReader fr = new FileReader(dir);  
            BufferedReader br = new BufferedReader(fr, 2048);  
            String memoryLine = br.readLine();  
            String subMemoryLine = memoryLine.substring(memoryLine  
                    .indexOf("MemTotal:"));  
            br.close();  
            long totalMemorySize = Integer.parseInt(subMemoryLine.replaceAll(  
                    "\\D+", ""));  
            long availableSize = getAvailableMemory(context) / 1024;  
            int percent = (int) ((totalMemorySize - availableSize)  
                    / (float) totalMemorySize * 100);  
            return percent + "%";  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        return "悬浮窗";  
    }  
  
    /** 
     * 获取当前可用内存，返回数据以字节为单位。 
     *  
     * @param context 
     *            可传入应用程序上下文。 
     * @return 当前可用内存。 
     */  
    private static long getAvailableMemory(Context context) {  
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();  
        getActivityManager(context).getMemoryInfo(mi);  
        return mi.availMem;  
    }  
  
}  
MyWindowManager是所有桌面悬浮窗的管理器，这里我们主要添加了createLauncher()、removeLauncher()和updateLauncher()这几个方法，分别用于创建、移除、以及更新火箭发射台悬浮窗。另外还添加了isReadyToLaunch()这个方法，它是用于判断小火箭是否已经拖动到火箭发射台上了。判断的方式当然也很简单，只需要对小火箭的边界和火箭发射台的边界进行检测，判断它们是否相交就行了。

接下来还需要修改FloatWindowSmallView中的代码，当手指拖动悬浮窗的时候要将它变成小火箭，如下所示：
[java] view plain copy 在CODE上查看代码片派生到我的代码片
public class FloatWindowSmallView extends LinearLayout {  
  
    /** 
     * 记录小悬浮窗的宽度 
     */  
    public static int windowViewWidth;  
  
    /** 
     * 记录小悬浮窗的高度 
     */  
    public static int windowViewHeight;  
  
    /** 
     * 记录系统状态栏的高度 
     */  
    private static int statusBarHeight;  
  
    /** 
     * 用于更新小悬浮窗的位置 
     */  
    private WindowManager windowManager;  
  
    /** 
     * 小悬浮窗的布局 
     */  
    private LinearLayout smallWindowLayout;  
  
    /** 
     * 小火箭控件 
     */  
    private ImageView rocketImg;  
  
    /** 
     * 小悬浮窗的参数 
     */  
    private WindowManager.LayoutParams mParams;  
  
    /** 
     * 记录当前手指位置在屏幕上的横坐标值 
     */  
    private float xInScreen;  
  
    /** 
     * 记录当前手指位置在屏幕上的纵坐标值 
     */  
    private float yInScreen;  
  
    /** 
     * 记录手指按下时在屏幕上的横坐标的值 
     */  
    private float xDownInScreen;  
  
    /** 
     * 记录手指按下时在屏幕上的纵坐标的值 
     */  
    private float yDownInScreen;  
  
    /** 
     * 记录手指按下时在小悬浮窗的View上的横坐标的值 
     */  
    private float xInView;  
  
    /** 
     * 记录手指按下时在小悬浮窗的View上的纵坐标的值 
     */  
    private float yInView;  
  
    /** 
     * 记录小火箭的宽度 
     */  
    private int rocketWidth;  
  
    /** 
     * 记录小火箭的高度 
     */  
    private int rocketHeight;  
  
    /** 
     * 记录当前手指是否按下 
     */  
    private boolean isPressed;  
  
    public FloatWindowSmallView(Context context) {  
        super(context);  
        windowManager = (WindowManager) context  
                .getSystemService(Context.WINDOW_SERVICE);  
        LayoutInflater.from(context).inflate(R.layout.float_window_small, this);  
        smallWindowLayout = (LinearLayout) findViewById(R.id.small_window_layout);  
        windowViewWidth = smallWindowLayout.getLayoutParams().width;  
        windowViewHeight = smallWindowLayout.getLayoutParams().height;  
        rocketImg = (ImageView) findViewById(R.id.rocket_img);  
        rocketWidth = rocketImg.getLayoutParams().width;  
        rocketHeight = rocketImg.getLayoutParams().height;  
        TextView percentView = (TextView) findViewById(R.id.percent);  
        percentView.setText(MyWindowManager.getUsedPercentValue(context));  
    }  
  
    @Override  
    public boolean onTouchEvent(MotionEvent event) {  
        switch (event.getAction()) {  
        case MotionEvent.ACTION_DOWN:  
            isPressed = true;  
            // 手指按下时记录必要数据,纵坐标的值都需要减去状态栏高度  
            xInView = event.getX();  
            yInView = event.getY();  
            xDownInScreen = event.getRawX();  
            yDownInScreen = event.getRawY() - getStatusBarHeight();  
            xInScreen = event.getRawX();  
            yInScreen = event.getRawY() - getStatusBarHeight();  
            break;  
        case MotionEvent.ACTION_MOVE:  
            xInScreen = event.getRawX();  
            yInScreen = event.getRawY() - getStatusBarHeight();  
            // 手指移动的时候更新小悬浮窗的状态和位置  
            updateViewStatus();  
            updateViewPosition();  
            break;  
        case MotionEvent.ACTION_UP:  
            isPressed = false;  
            if (MyWindowManager.isReadyToLaunch()) {  
                launchRocket();  
            } else {  
                updateViewStatus();  
                // 如果手指离开屏幕时，xDownInScreen和xInScreen相等，且yDownInScreen和yInScreen相等，则视为触发了单击事件。  
                if (xDownInScreen == xInScreen && yDownInScreen == yInScreen) {  
                    openBigWindow();  
                }  
            }  
            break;  
        default:  
            break;  
        }  
        return true;  
    }  
  
    /** 
     * 将小悬浮窗的参数传入，用于更新小悬浮窗的位置。 
     *  
     * @param params 
     *            小悬浮窗的参数 
     */  
    public void setParams(WindowManager.LayoutParams params) {  
        mParams = params;  
    }  
  
    /** 
     * 用于发射小火箭。 
     */  
    private void launchRocket() {  
        MyWindowManager.removeLauncher(getContext());  
        new LaunchTask().execute();  
    }  
  
    /** 
     * 更新小悬浮窗在屏幕中的位置。 
     */  
    private void updateViewPosition() {  
        mParams.x = (int) (xInScreen - xInView);  
        mParams.y = (int) (yInScreen - yInView);  
        windowManager.updateViewLayout(this, mParams);  
        MyWindowManager.updateLauncher();  
    }  
  
    /** 
     * 更新View的显示状态，判断是显示悬浮窗还是小火箭。 
     */  
    private void updateViewStatus() {  
        if (isPressed && rocketImg.getVisibility() != View.VISIBLE) {  
            mParams.width = rocketWidth;  
            mParams.height = rocketHeight;  
            windowManager.updateViewLayout(this, mParams);  
            smallWindowLayout.setVisibility(View.GONE);  
            rocketImg.setVisibility(View.VISIBLE);  
            MyWindowManager.createLauncher(getContext());  
        } else if (!isPressed) {  
            mParams.width = windowViewWidth;  
            mParams.height = windowViewHeight;  
            windowManager.updateViewLayout(this, mParams);  
            smallWindowLayout.setVisibility(View.VISIBLE);  
            rocketImg.setVisibility(View.GONE);  
            MyWindowManager.removeLauncher(getContext());  
        }  
    }  
  
    /** 
     * 打开大悬浮窗，同时关闭小悬浮窗。 
     */  
    private void openBigWindow() {  
        MyWindowManager.createBigWindow(getContext());  
        MyWindowManager.removeSmallWindow(getContext());  
    }  
  
    /** 
     * 用于获取状态栏的高度。 
     *  
     * @return 返回状态栏高度的像素值。 
     */  
    private int getStatusBarHeight() {  
        if (statusBarHeight == 0) {  
            try {  
                Class<?> c = Class.forName("com.android.internal.R$dimen");  
                Object o = c.newInstance();  
                Field field = c.getField("status_bar_height");  
                int x = (Integer) field.get(o);  
                statusBarHeight = getResources().getDimensionPixelSize(x);  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
        }  
        return statusBarHeight;  
    }  
  
    /** 
     * 开始执行发射小火箭的任务。 
     *  
     * @author guolin 
     */  
    class LaunchTask extends AsyncTask<Void, Void, Void> {  
  
        @Override  
        protected Void doInBackground(Void... params) {  
            // 在这里对小火箭的位置进行改变，从而产生火箭升空的效果  
            while (mParams.y > 0) {  
                mParams.y = mParams.y - 10;  
                publishProgress();  
                try {  
                    Thread.sleep(8);  
                } catch (InterruptedException e) {  
                    e.printStackTrace();  
                }  
            }  
            return null;  
        }  
  
        @Override  
        protected void onProgressUpdate(Void... values) {  
            windowManager.updateViewLayout(FloatWindowSmallView.this, mParams);  
        }  
  
        @Override  
        protected void onPostExecute(Void result) {  
            // 火箭升空结束后，回归到悬浮窗状态  
            updateViewStatus();  
            mParams.x = (int) (xDownInScreen - xInView);  
            mParams.y = (int) (yDownInScreen - yInView);  
            windowManager.updateViewLayout(FloatWindowSmallView.this, mParams);  
        }  
  
    }  
  
}  
这里在代码中添加了一个isPressed标识位，用于判断用户是否正在拖动悬浮窗。当拖动的时候就调用updateViewStatus()方法来更新悬浮窗的显示状态，这时悬浮窗就会变成一个小火箭。然后当手指离开屏幕的时候，也会调用updateViewStatus()方法，这时发现isPressed为false，就会将悬浮窗重新显示出来。

同时，当手指离开屏幕的时候，还会调用MyWindowManager的isReadyToLaunch()方法来判断小火箭是否被拖动到火箭发射台上了，如果为true，就会触发火箭升空的动画效果。火箭升空的动画实现是写在LaunchTask这个任务里的，可以看到，这里会在doInBackground()方法中执行耗时逻辑，将小火箭的纵坐标不断减小，以让它实现上升的效果。当纵坐标减小到0的时候，火箭升空的动画就结束了，然后在onPostExecute()方法中重新将悬浮窗显示出来。

另外，在AndroidManifest.xml文件中记得要声明两个权限，如下所示：
[html] view plain copy 在CODE上查看代码片派生到我的代码片
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />  
<uses-permission android:name="android.permission.GET_TASKS" />  
代码就只有这么多，接下来我们运行一下看看效果吧。在主界面点击Start Float Window按钮可以开启悬浮窗并回到桌面，然后拖动悬浮窗后就会变成小火箭的状态，将它拖动到屏幕底部火箭发射台上，然后放手，小火箭就会腾空而起了，如下图所示：



好了，今天的讲解就到这里，伴随着小火箭的起飞，我今年的最后一篇文章也结束了。

新的一年即将来临，祝愿大家在未来的一年里，无论是工作还是学习，都能像这个小火箭一样，腾飞起来，达到一个新的高度！2014年，我们继续共同努力！

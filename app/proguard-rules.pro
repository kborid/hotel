# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\LI\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html

#==================common=====================
#代码混淆压缩比，在0~7之间，默认为5，一般不需要修改
-optimizationpasses 5

#混淆时不使用大小写混合，混淆后的类名为小写
-dontusemixedcaseclassnames

#指定不去忽略非公共的库的类
-dontskipnonpubliclibraryclasses

#指定不去忽略非公共的库的类的成员
-dontskipnonpubliclibraryclassmembers

#不做预校验，preverify是proguard的四个步骤之一
#Android不需要preverify，去掉这一步可加快混淆速度
-dontpreverify

#有了verbose这句话，混淆后就会生成映射文件
#包含有类名->混淆后类名的映射关系
#然后使用printmapping指定映射文件的名称
-verbose
#-printmapping proguardMapping.txt

#指定混淆时采用的算法，后面的参数是一个过滤器
#这个过滤器是google推荐的算法，一般不改变
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

#保护代码中的Annotation不被混淆
#这在JSON实体映射时非常重要，比如fastJson
-keepattributes *Annotation*

#避免混淆泛型
#这在JSON实体映射时非常重要，比如fastJson
-keepattributes Signature

#抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable

#保留所有的本地native方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}

#保留了继承自Activity，Application这些类的子类
#因为这些子类都有可能被外部调用
#比如说，第一行就保证了所有的Activity的子类不要被混淆
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class com.android.vending.licensing.ILicensingService
-keep public class com.google.vending.licensing.ILicensingService

#如果有引用android-support-v4.jar包，可以添加下面这行
#-keep public class com.tuniu.app.ui.fragment.** {*;}

#保留在Activity中的方法参数是view的方法，
#从而我们在layout里面编写onClick就不会被影响
-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}

#保持枚举 enum类不被混淆
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#保持自定义控件（继承自View）不被混淆
-keepclassmembers public class * extends android.view.View {
   *** get*();
   void set*(***);
   public <init>(android.content.Context);
   public <init>(android.content.Context, android.util.AttributeSet);
   public <init>(android.content.Context, android.util.AttributeSet, int);
}

#保持序列化的类Parcelable不被混淆
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

#保持Serializable序列化的类不被混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamFileld[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

#对于R资源下的所有类及方法，都不能被混淆
-keep class **.R$* {
    *;
}

#对于带有回调函数onXXEvent的，不能被混淆
-keepclassmembers class * {
    void *(**On*Event);
}

#==================common end=====================

#gson
-dontwarn com.google.**
-keep class com.google.gson.** {*;}

#protobuf
-dontwarn com.google.**
-keep class com.google.protobuf.** {*;}

#fastjson
-dontwarn com.alibaba.fastjson.**
-keep class com.alibaba.fastjson.** { *; }

#内部类
-keepattributes InnerClasses
-keep class **.R$* {
    <fields>;
}

# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
#针对support-v4.jar，混淆策略
#-libraryjars libs/android-support-v4.jar
#-dontwarn android.support.v4.**
#-keep class android.support.v4.** {*;}
#-keep interface android.support.v4.app.** {*;}
#-keep public class * extends android.support.v4.**
#-keep public class * extends android.app.Fragment

-keep class org.apache.http.** { *; }
-keep class android.net.http.** { *; }
-dontwarn org.apache.http.**
-dontwarn android.net.http.**

#webview基本混淆
-keepclassmembers class * extends android.webkit.webViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.webViewClient {
    public void *(android.webkit.WebView, java.lang.String);
}

#webview相关兼容扩展混淆
-dontwarn com.prj.sdk.widget.webview.**
-keep class com.prj.sdk.widget.webview.** { *; }

#实体类，不需要混淆
-keep class com.huicheng.hotel.android.net.bean.** {*;}
-keepclassmembers class * {
public <methods>;
public <fields>;
}

#===============第三方jar包=======================

#友盟统计sdk
-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}

#友盟分享start
-dontusemixedcaseclassnames
    -dontshrink
    -dontoptimize
    -dontwarn com.google.android.maps.**
    -dontwarn android.webkit.WebView
    -dontwarn com.umeng.**
    -dontwarn com.tencent.weibo.sdk.**
    -dontwarn com.facebook.**
    -keep public class javax.**
    -keep public class android.webkit.**
    -dontwarn android.support.v4.**
    -keep enum com.facebook.**
    -keepattributes Exceptions,InnerClasses,Signature
    -keepattributes *Annotation*
    -keepattributes SourceFile,LineNumberTable

    -keep public interface com.facebook.**
    -keep public interface com.tencent.**
    -keep public interface com.umeng.socialize.**
    -keep public interface com.umeng.socialize.sensor.**
    -keep public interface com.umeng.scrshot.**
    -keep class com.android.dingtalk.share.ddsharemodule.** { *; }
    -keep public class com.umeng.socialize.* {*;}


    -keep class com.facebook.**
    -keep class com.facebook.** { *; }
    -keep class com.umeng.scrshot.**
    -keep public class com.tencent.** {*;}
    -keep class com.umeng.socialize.sensor.**
    -keep class com.umeng.socialize.handler.**
    -keep class com.umeng.socialize.handler.*
    -keep class com.umeng.weixin.handler.**
    -keep class com.umeng.weixin.handler.*
    -keep class com.umeng.qq.handler.**
    -keep class com.umeng.qq.handler.*
    -keep class UMMoreHandler{*;}
    -keep class com.tencent.mm.sdk.modelmsg.WXMediaMessage {*;}
    -keep class com.tencent.mm.sdk.modelmsg.** implements   com.tencent.mm.sdk.modelmsg.WXMediaMessage$IMediaObject {*;}
    -keep class im.yixin.sdk.api.YXMessage {*;}
    -keep class im.yixin.sdk.api.** implements im.yixin.sdk.api.YXMessage$YXMessageData{*;}
    -keep class com.tencent.mm.sdk.** {
     *;
    }
    -keep class com.tencent.mm.opensdk.** {
   *;
    }
    -dontwarn twitter4j.**
    -keep class twitter4j.** { *; }

    -keep class com.tencent.** {*;}
    -dontwarn com.tencent.**
    -keep public class com.umeng.com.umeng.soexample.R$*{
    public static final int *;
    }
    -keep public class com.linkedin.android.mobilesdk.R$*{
    public static final int *;
        }
    -keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    }

    -keep class com.tencent.open.TDialog$*
    -keep class com.tencent.open.TDialog$* {*;}
    -keep class com.tencent.open.PKDialog
    -keep class com.tencent.open.PKDialog {*;}
    -keep class com.tencent.open.PKDialog$*
    -keep class com.tencent.open.PKDialog$* {*;}

    -keep class com.sina.** {*;}
    -dontwarn com.sina.**
    -keep class  com.alipay.share.sdk.** {
       *;
    }
    -keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
    }

    -keep class com.linkedin.** { *; }
    -keepattributes Signature
#友盟分享end

#okhttp, okio混淆start
-dontwarn com.squareup.okhttp.**
-keep class com.squareup.okhttp.** { *;}
-dontwarn okio.**
#okhttp混淆end

#jpush混淆start
-dontoptimize
-dontpreverify
-dontwarn cn.jpush.**
-keep class cn.jpush.** { *; }
-dontwarn cn.jiguang.**
-keep class cn.jiguang.** { *; }
#jpush混淆end


#支付宝start
-keep class com.alipay.android.app.IAlixPay{*;}
-keep class com.alipay.android.app.IAlixPay$Stub{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback$Stub{*;}
-keep class com.alipay.sdk.app.PayTask{ public *;}
-keep class com.alipay.sdk.app.AuthTask{ public *;}
-keep class com.alipay.mobilesecuritysdk.*
-dontwarn com.alipay.apmobilesecuritysdk.face**
-keep class com.alipay.apmobilesecuritysdk.face.**{*;}
-dontwarn android.net.SSLCertificateSocketFactory
-keep class com.huicheng.hotel.android.pay.alipay.PayResult
#支付宝end


#---- 高德Map 混淆---
#定位
-keep class com.amap.api.location.**{*;}
-dontwarn com.amap.api.location.**
-keep class com.amap.api.fence.**{*;}
-dontwarn com.amap.api.fence.**
-keep class com.autonavi.aps.amapapi.model.**{*;}
-dontwarn com.autonavi.aps.amapapi.model.**
#2D地图
-keep class com.amap.api.maps2d.**{*;}
-dontwarn com.amap.api.maps2d.**
-keep class com.amap.api.mapcore2d.**{*;}
-dontwarn com.amap.api.mapcore2d.**
#3D地图
-keep class com.amap.api.maps.**{*;}
-keep class com.autonavi.amap.mapcore.*{*;}
-keep class com.amap.api.trace.**{*;}
#导航
-keep class com.amap.api.navi.**{*;}
-keep class com.autonavi.**{*;}
#搜索
-keep class com.amap.api.services.**{*;}

#-------pinyin------
-dontwarn demo.**
-keep class demo.**{*;}

-dontwarn net.sourceforge.pinyin4j.**
-keep class net.sourceforge.pinyin4j.**{*;}
-keep class net.sourceforge.pinyin4j.format.**{*;}
-keep class net.sourceforge.pinyin4j.format.exception.**{*;}

#------open install-------
#忽略警告
-dontwarn com.fm.openinstall.**
#避免混淆
-keep public class com.fm.openinstall.* {*; }

-dontwarn com.squareup.haha.**
-keep public class com.squareup.haha.*{*;}

#科大讯飞
-keep class com.iflytek.**{*;}

#银联在线
-dontwarn org.simalliance.openmobileapi.**

#Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

#全民付
-dontwarn com.chinaums.pppay.**
-keep public class com.chinaums.pppay.*{*;}

#全民花
#-dontwarn com.dgonlam.**
#-dontwarn org.apache.**
#-dontwarn android.**
#-dontwarn com.tencent.smtt.**
#-keep class com.dgonlam.**{*;}
#-keep class com.ums.xutils.**{*;}
#-keep class com.ums.zxing.**{*;}
#-keep class com.tencent.smtt.**{*;}
#-keep class com.csii.powerenter**{*;}
#-keep class com.github.Dgonlam.**{*;}
#-keep class chihane.**{*;}
#-keep class com.raizlabs.**{*;}
#-keep class dgonlam.**{*;}
#-keep class mlxy.utils.**{*;}
#-keep class com.ums.iou.ui.**{*;}
#-keep class com.ums.iou.entry.LimitRequestUtils{*;}
#-keep class com.ums.iou.entry.OnLimitRequestResultListener{*;}
#-keep class com.ums.iou.netapi.XUtilsHttpClient{*;}
#-keep class com.ums.iou.entity.**{*;}
#-keep class org.apache.**{*;}

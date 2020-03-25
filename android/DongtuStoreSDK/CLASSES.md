# 类和开放接口

----------------

## 管理类

### DongtuStore

Android SDK核心文件

* 注册appId和appSecret，初始化配置，建议在Application.onCreate()中调用。

```java
public static void initConfig(Context context, String appId, String appSecret)
```

* 传入用户信息。

```java
public static void setUserInfo(String userId, String userName, DTGender gender, String address, String email, String phone, JSONObject extra) 
```

* 加载SDK功能，和`destroy`对应。对它的调用是可选的。

```java
public static void load()
```

* 当一段时间内不需要再使用SDK时，可以调用此函数。销毁单例，节省一些资源。

```java
public static void destroy()
```

* 传入表情键盘控件。

```java
public static void setKeyboard(DTStoreKeyboard keyboard)
```

* 传入编辑框控件。

```java
public static void setEditText(DTStoreEditView editText)
```

* 开启联想功能。

```java
public static void setupSearchPopupAboveView(View anchor, DTStoreEditView input)
```

* 设置消息发送回调监听。

```java
public static void setSendMessageListener(DongtuStoreSendMessageListener listener)
```

* 自定义SDK控件中某些Unicode Emoji表情的外观。

```java
public static void setUnicodeEmojiDrawableProvider(DTStoreUnicodeEmojiDrawableProvider provider)
```

* 自定义键盘中显示的Unicode Emoji表情集合。

    * `unicodes` 数组含有是要显示在键盘中每个表情的Unicode编号。大部分Unicode字符都用一个编号表示，在数组的对应个位置放入int即可。对于极少数用两个编号表示的字符（主要是各国国旗），就需要放入int[]。

```java
public static void setUnicodeEmojiSet(Object[] unicodes)
```

* 自定义键盘中Unicode Emoji标签页的icon。

```java
public static void setUnicodeEmojiTabIconProvider(DTStoreSendMessageListener provider)
```

* 自定义Gif键盘的部分样式

```java
public static void setGifKeyboardConfigProvider(DTGifKeyboardConfigProvider provider)
```

* 自定义收藏管理界面的部分样式

```java
public static void setCollectionManagerConfigProvider(DTStoreCollectionManagerConfigProvider provider)
```

* 设置状态栏文字颜色。当App启用了沉浸式UI，并且将状态栏的背景设置为浅色时，可以调用此方法，传入true以使状态栏文字变为深色。

```java
public static void setEnableLightStatusBar(boolean enabled)
```

* 展示动图

```java
public static void loadImageInto(DTImageView imageView, final String image, final String id, final int width, final int height)
```

* 展示Gif搜索模块

```java
public static void showDongtuPopup(Activity activity)
```

* 判断商店表情是否已收藏

```java
public static void collectionHasSticker(String code, CollectionExistsCallback callback)
```

* 收藏商店表情

```java
public static void collectSticker(String code, DTOutcomeListener listener)
```

* 移除收藏的商店表情

```java
public static void removeCollectedSticker(String code, DTOutcomeListener listener)
```

* 判断动图是否已收藏

```java
public static void collectionHasGif(String code, CollectionExistsCallback callback)
```

* 收藏动图表情

```java
public static void collectGif(String id, DTOutcomeListener listener)
```

* 移除收藏的动图表情

```java
public static void removeCollectedGif(String id, DTOutcomeListener listener)
```

## UI定制相关类

### DTStoreUnicodeEmojiDrawableProvider

实现这个接口的类，可以替换SDK控件中某些Unicode Emoji表情的外观。

* 根据收到的Unicode Code Point，返回对应的Drawable或者null（表示该Code Point不需要替换外观）

```java
Drawable getDrawableFromCodePoint(int codePoint);
```

### DTStoreUnicodeEmojiTabIconProvider

实现这个接口的类，可以为表情键盘的Unicode Emoji标签页提供缩略图，展示在键盘底部的tab列表中。

```java
Drawable getIcon();
```

### DTGifKeyboardConfigProvider

实现这个接口的类，可以定义Gif键盘中的热词标签和搜索文字输入框的字体、颜色、边框等UI样式。

### DTStoreCollectionManagerConfigProvider

实现这个接口的类，可以定义收藏管理界面下方两个按钮的字体颜色。

## UI控件

### DTStoreSendButton

消息发送按钮，继承自`android.widget.Button`。

### DTStoreEditView

消息输入控件，继承`android.widget.EditText`。

* 设置Emoji相对于普通字符显示大小的比例（仅对通过DTStoreUnicodeEmojiDrawableProvider自定义了外观的Emoji有效）。

```java
setUnicodeEmojiSpanSizeRatio(float ratio)
```

* 设置与自定义Emoji展示图片相冲突的Span类。为了实现将DTStoreUnicodeEmojiDrawableProvider提供的Drawable显示在文本框里，SDK给文本框中对应的文字设置了Span。当用户想要自己设置一些Span时，可能会和SDK设置的Span形成冲突，这时就可以将准备自行设置到文字中去的Span类型用此方法告诉文本框，SDK会在设置Span时跳过它们。

```java
setConflictSpanClasses(Class[] classes)
```

### DTStoreKeyboard.java

表情键盘控件，继承`android.widget.RelativeLayout`。

### DTStoreMessageView

消息显示控件。用于展示表情商店特有的文本消息和表情消息。继承`RelativeLayout`。

需要说明的是，本控件使用了内部的一个`TextView`进行文本消息的展示。开发者通常习惯于在xml布局中通过一些属性设置文字的样式，但在本控件上，需要利用style应用想设置的属性。具体方法有两种：

1. 自己写一个要给`TextView`使用的style，使用带style的构造函数实例化本控件，传入其id；
2. 在项目中写一个名为DTStoreMessageView的style，它会被DTStoreMessageView自动找到并使用。

* 带style的构造函数。用于在代码中实例化时指定文本的样式。

```java
public DTStoreMessageView(@NonNull Context context, @StyleRes int textStyle)
```

* 设置大表情图片显示大小。

```java
public void setStickerSize(int size)
```

* 显示大表情。

```java
public void showSticker(String code)
```

* 显示文本消息。

```java
public void showText(String text)
```

* 设置Emoji相对于普通字符显示大小的比例（仅对DTStoreUnicodeEmojiDrawableProvider提供了图片的Emoji有效）。

```java
public void setUnicodeEmojiSpanSizeRatio(float ratio)
```

* 设置与自定义Emoji展示图片相冲突的Span类。描述见DTStoreEditView中的同名方法。

```java
public void setConflictSpanClasses(Class[] classes)
```

## 监听回调

### DTStoreSendMessageListener

表情消息发送监听接口。

* 发送大表情

```java
void onSendSticker(DTStoreSticker sticker)
```

* 发送动图Gif

```java
void onSendDTImage(DTImage image)
```

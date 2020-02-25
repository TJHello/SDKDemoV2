package com.eyewind.sdk.demo.v2

import android.graphics.Bitmap
import android.graphics.Rect
import android.widget.ImageView
import java.io.File
import androidx.core.graphics.drawable.DrawableCompat
import android.graphics.drawable.AnimationDrawable
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


/**
 * 作者:TJbaobao
 * 时间:2019/8/16  11:14
 * 说明:
 * 使用：
 */
class KotlinCodeSugar {


}


inline fun File.foreachFile(function: (File) -> Unit){
    val fileList = listFiles()
    if(fileList!=null){
        for(file in fileList){
            function(file)
        }
    }
}

inline fun Rect.set(bitmap: Bitmap){
    set(0,0,bitmap.width,bitmap.height)
}

fun ImageView.tint(color:Int){

    val up = this.drawable
    val drawableUp = DrawableCompat.wrap(up!!)
    DrawableCompat.setTint(drawableUp,color)
    this.setImageDrawable(drawableUp)

//    val up1 = this.drawable
//    val drawableUp1 = DrawableCompat.unwrap<Drawable>(up1!!)
//    DrawableCompat.setTintList(drawableUp1, null)
}

fun ViewGroup.foreachChild(function: (View) -> Unit){
    val childCount = this.childCount
    for(i in 0 until childCount){
        val child = this.getChildAt(i)
        if(child is ViewGroup){
            child.foreachChild(function)
        }else{
            function(child)
        }
    }
}

inline fun <reified  T>MutableList<T>.downForeach(function: (item:T) -> Unit){
    for(i in size-1 downTo 0){
        function(get(i))
    }
}

fun ImageView.startFrameAnim(){
    val drawable = this.drawable
    if(drawable is AnimationDrawable){
        drawable.start()
    }
}

inline fun <reified T> Gson.fromJson(jsonStr:String?):T?{
    return fromJson(jsonStr, genericType<T>())
}

inline fun <reified T> genericType() = object: TypeToken<T>() {}.type


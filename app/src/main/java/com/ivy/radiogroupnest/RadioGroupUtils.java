package com.ivy.radiogroupnest;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by ivy on 2017/3/17.
 * Description：
 */

public class RadioGroupUtils {
    private RadioGroup mRG;
    //用于给rb设置OnCheckChangeListener
    private Method mSetOnCheckChangeListenerMethod;
    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener;
    private OnHierarchyChangeHelpListener mOnHierarchyChangeHelpListener=new OnHierarchyChangeHelpListener();
    private Field mOnHierarchyChangeListenerField;
    private CompoundButton.OnCheckedChangeListener cancelCheckedChangeListener;
    public RadioGroupUtils(RadioGroup rg){
        this.mRG = rg;
    }

    public void supportNest(){
        mRG.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                if (parent== mRG && child instanceof RadioButton)
                    return;
                dispatchChildViewAdded(child);
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
                if (parent== mRG &&child instanceof RadioButton)
                    return;
                dispatchChildViewRemoved(child);
            }
        });
        traversalSetOnCheckedChangeWidgetListener(mRG,false);
    }

    private void dispatchChildViewAdded(View child) {
        traversalSetOnCheckedChangeWidgetListener(child,false);
    }

    private void dispatchChildViewRemoved(View child) {
        traversalSetOnCheckedChangeWidgetListener(child,true);
    }

    private void traversalSetOnCheckedChangeWidgetListener(View parent, boolean isRemove) {
        if (parent instanceof ViewGroup){
            //rg不进行处理，当前rg除外
            if (parent instanceof RadioGroup && parent!=mRG) {
                return;
            }
            addHierarchyChangeListener((ViewGroup) parent,isRemove);
            int count=((ViewGroup)parent).getChildCount();
            for(int i=0;i<count;i++){
                traversalSetOnCheckedChangeWidgetListener(((ViewGroup) parent).getChildAt(i),isRemove);
            }
        }else{
            setOnCheckedChangeWidgetListener(parent,isRemove);
        }
    }

    private void addHierarchyChangeListener(ViewGroup parent, boolean isRemove) {
        //当前rg不需要处理onHierarchyChangeListener
        if (parent==mRG)
            return;
        //parent.setOnHierarchyChangeListener(isRemove?null:mOnHierarchyChangeHelpListener);
        ViewGroup.OnHierarchyChangeListener onHierarchyChangeListener=getOnHierarchyChangeListener(parent);
        if (isRemove){
            if (onHierarchyChangeListener instanceof OnHierarchyChangeProxyListener)
                parent.setOnHierarchyChangeListener(((OnHierarchyChangeProxyListener) onHierarchyChangeListener).getOnHierarchyChangeListener());
            else
                parent.setOnHierarchyChangeListener(null);
        }else{
            if (onHierarchyChangeListener==null)
                parent.setOnHierarchyChangeListener(mOnHierarchyChangeHelpListener);
            else
                parent.setOnHierarchyChangeListener(new OnHierarchyChangeProxyListener(onHierarchyChangeListener));
        }
    }

    private ViewGroup.OnHierarchyChangeListener getOnHierarchyChangeListener(ViewGroup viewGroup){
        try{
            if (mOnHierarchyChangeListenerField==null) {
                Class<ViewGroup> clazz = ViewGroup.class;
                mOnHierarchyChangeListenerField = clazz.getDeclaredField("mOnHierarchyChangeListener");
                mOnHierarchyChangeListenerField.setAccessible(true);
            }
            return (ViewGroup.OnHierarchyChangeListener) mOnHierarchyChangeListenerField.get(viewGroup);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private void setOnCheckedChangeWidgetListener(View child, boolean isRemove) {
        if (!(child instanceof RadioButton))
            return;
        if (!isRemove) {
            int id = child.getId();
            // generates an id if it's missing
            if (id == View.NO_ID) {
                id = View.generateViewId();
                child.setId(id);
            }
        }
        setRBOnCheckedChangeWidgetListener(child,isRemove);
    }

    private void setRBOnCheckedChangeWidgetListener(View child, boolean isRemove){
        try {
            if (mSetOnCheckChangeListenerMethod ==null||mOnCheckedChangeListener==null){
                Class<RadioGroup> clazzRg=RadioGroup.class;
                Class<CompoundButton> clazz=CompoundButton.class;
                Field field=clazzRg.getDeclaredField("mChildOnCheckedChangeListener");
                field.setAccessible(true);
                mOnCheckedChangeListener= (CompoundButton.OnCheckedChangeListener) field.get(mRG);
                mSetOnCheckChangeListenerMethod =clazz.getDeclaredMethod("setOnCheckedChangeWidgetListener", CompoundButton.OnCheckedChangeListener.class);
                mSetOnCheckChangeListenerMethod.setAccessible(true);
            }
            if (isRemove)
                mSetOnCheckChangeListenerMethod.invoke(child,cancelCheckedChangeListener);
            else
                mSetOnCheckChangeListenerMethod.invoke(child,mOnCheckedChangeListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class OnHierarchyChangeHelpListener implements ViewGroup.OnHierarchyChangeListener{

        @Override
        public void onChildViewAdded(View parent, View child) {
            dispatchChildViewAdded(child);
            if (mOnHierarchyChangeListener!=null){
                mOnHierarchyChangeListener.onChildViewAdded(parent,child);
            }
        }

        @Override
        public void onChildViewRemoved(View parent, View child) {
            dispatchChildViewRemoved(child);
            if (mOnHierarchyChangeListener!=null){
                mOnHierarchyChangeListener.onChildViewRemoved(parent,child);
            }
        }
    }

    /**
     * 存在OnHierarchyChangeListener 设置代码，保持原有特性
     */
    private class OnHierarchyChangeProxyListener extends OnHierarchyChangeHelpListener{
        private ViewGroup.OnHierarchyChangeListener mOnHierarchyChangeListenerOrigin;
        public OnHierarchyChangeProxyListener(ViewGroup.OnHierarchyChangeListener onHierarchyChangeListener){
            this.mOnHierarchyChangeListenerOrigin=onHierarchyChangeListener;
        }
        @Override
        public void onChildViewAdded(View parent, View child) {
            if (mOnHierarchyChangeListenerOrigin!=null)
                this.mOnHierarchyChangeListenerOrigin.onChildViewAdded(parent,child);
            super.onChildViewAdded(parent, child);
        }

        @Override
        public void onChildViewRemoved(View parent, View child) {
            if (mOnHierarchyChangeListenerOrigin!=null)
                this.mOnHierarchyChangeListenerOrigin.onChildViewRemoved(parent,child);
            super.onChildViewRemoved(parent, child);
        }

        public ViewGroup.OnHierarchyChangeListener getOnHierarchyChangeListener(){
            return mOnHierarchyChangeListenerOrigin;
        }
    }

    private ViewGroup.OnHierarchyChangeListener mOnHierarchyChangeListener;
    public void setOnHierarchyChangeListener(ViewGroup.OnHierarchyChangeListener onHierarchyChangeListener){
        this.mOnHierarchyChangeListener=onHierarchyChangeListener;
    }
}

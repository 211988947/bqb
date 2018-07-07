package com.ihewro.android_expression_package.adapter;

import android.support.annotation.Nullable;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.ihewro.android_expression_package.R;
import com.ihewro.android_expression_package.bean.Expression;
import com.ihewro.android_expression_package.util.UIUtil;

import java.util.List;

import pl.droidsonroids.gif.GifImageView;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2018/04/06
 *     desc   : 用来显示表情列表的数据适配器
 *     version: 1.0
 * </pre>
 */
public class ExpressionListAdapter extends BaseQuickAdapter<Expression, ExpressionListAdapter.IViewHolder> {

    private List<Expression> expressionList;

    /**
     * 控制是否显示Checkbox
     */
    private boolean showCheckBox = false;

    /**
     * 防止Checkbox错乱 做setTag  getTag操作
     */
    private SparseBooleanArray mCheckStates = new SparseBooleanArray();

    /**
     *
     * @param layoutResId
     * @param data
     */
    public ExpressionListAdapter(int layoutResId, @Nullable List<Expression> data) {
        super(layoutResId, data);
        expressionList = data;
    }

    @Override
    protected void convert(final ExpressionListAdapter.IViewHolder helper, Expression item) {

        final CheckBox checkBox = helper.getView(R.id.cb_item);
        checkBox.setTag(helper.getAdapterPosition());
        UIUtil.setImageToImageView(item.getStatus(),item.getUrl(), (GifImageView) helper.getView(R.id.iv_expression));
        //判断当前checkbox的状态
        if (showCheckBox) {
            helper.getView(R.id.cb_item).setVisibility(View.VISIBLE);
            //防止显示错乱
            ((CheckBox)helper.getView(R.id.cb_item)).setChecked(mCheckStates.get(helper.getAdapterPosition(), false));
        } else {
            helper.getView(R.id.cb_item).setVisibility(View.GONE);
            //取消掉Checkbox后不再保存当前选择的状态
            ((CheckBox)helper.getView(R.id.cb_item)).setChecked(false);
            mCheckStates.clear();
        }

        //对checkbox的监听 保存选择状态 防止checkbox显示错乱
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                int pos = (int) compoundButton.getTag();
                if (b) {
                    mCheckStates.put(pos, true);
                } else {
                    mCheckStates.delete(pos);
                }

            }
        });
    }


    public static class IViewHolder extends BaseViewHolder{

        GifImageView IvExpression;

        public IViewHolder(View itemView) {
            super(itemView);
            IvExpression = itemView.findViewById(R.id.iv_expression);
        }
    }
    public void setShowCheckBox(boolean showCheckBox) {
        this.showCheckBox = showCheckBox;
    }

}

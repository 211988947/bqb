package com.ihewro.android_expression_package.fragment;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.ihewro.android_expression_package.R;
import com.ihewro.android_expression_package.adapter.ExpressionListAdapter;
import com.ihewro.android_expression_package.bean.Expression;
import com.ihewro.android_expression_package.util.FileUtil;
import com.ihewro.android_expression_package.util.UIUtil;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.context.IconicsLayoutInflater2;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import pl.droidsonroids.gif.GifImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExpressionContentFragment extends Fragment {


    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    Unbinder unbinder;

    private ExpressionListAdapter adapter;
    private List<Expression> expressionList = new ArrayList<>();
    private String tabName;
    private MaterialDialog expressionDialog;
    private int currentPosition = -1;

    //自定义布局
    GifImageView ivExpression;
    TextView tvExpression;
    View save;
    View share;
    View timShare;
    View weChatShare;
    View qqShare;


    public ExpressionContentFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expression_content, container, false);
        unbinder = ButterKnife.bind(this, view);

        //初始化弹出层相关信息
        initView();

        initExpressionDialogListener();

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        GridLayoutManager gridLayoutManager =  new GridLayoutManager(getActivity(),5);
        recyclerView.setLayoutManager(gridLayoutManager);

        initExpressionData();

        adapter = new ExpressionListAdapter(R.layout.item_expression,expressionList);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                currentPosition = position;
                Expression expression = expressionList.get(position);
                UIUtil.setImageToImageView(expression.getStatus(),expression.getUrl(),ivExpression);
                tvExpression.setText(expression.getName());
                expressionDialog.show();

            }
        });
    }


    /**
     * 初始化表情弹出框监听器
     */
    private void initExpressionDialogListener(){
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //保存图片到sd卡
                if (currentPosition != -1){
                    FileUtil.saveImageToGallery(UIUtil.getContext(), ivExpression.getDrawable(),tabName,expressionList.get(currentPosition).getName());
                }
            }
        });

    }

    /**
     * 初始化表情包数据
     */
    private void initExpressionData(){
        Bundle bundle = getArguments();
        expressionList = (List<Expression>) bundle.getSerializable("data");
        tabName = bundle.getString("name");
    }



    private void initView(){
        expressionDialog  = new MaterialDialog.Builder(getActivity())
                .customView(R.layout.item_show_expression, true)
                .build();

        View view = expressionDialog.getCustomView();
        ivExpression = view.findViewById(R.id.expression_image);
        tvExpression = view.findViewById(R.id.expression_name);
        save = view.findViewById(R.id.save_image);
        share = view.findViewById(R.id.share);
        timShare = view.findViewById(R.id.tim_share);
        weChatShare = view.findViewById(R.id.weChat_share);
        qqShare = view.findViewById(R.id.qq_share);



    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}

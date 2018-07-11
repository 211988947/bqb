package com.ihewro.android_expression_package.fragment;


import android.content.Entity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blankj.ALog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihewro.android_expression_package.R;
import com.ihewro.android_expression_package.adapter.ExpressionListAdapter;
import com.ihewro.android_expression_package.bean.EventMessage;
import com.ihewro.android_expression_package.bean.Expression;
import com.ihewro.android_expression_package.view.ExpImageDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExpressionContentFragment extends Fragment {


    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    Unbinder unbinder;

    private ExpressionListAdapter adapter;
    private List<Expression> expressionList = new ArrayList<>();
    private ExpImageDialog expressionDialog;
    private int currentPosition = -1;
    View notDataView;
    private String tabName;


    public static Fragment fragmentInstant(String data,String name){
        ExpressionContentFragment fragment = new ExpressionContentFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("data", data);//json字符串
        bundle.putString("name",name);
        fragment.setArguments(bundle);
        return fragment;
    }


    public ExpressionContentFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expression_content, container, false);
        unbinder = ButterKnife.bind(this, view);
        notDataView = getLayoutInflater().inflate(R.layout.item_empty_view, (ViewGroup) recyclerView.getParent(), false);

        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }


        //初始化弹出层相关信息
        initView();


        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ALog.d("重新创建了该fragment");

        GridLayoutManager gridLayoutManager =  new GridLayoutManager(getActivity(),4);
        recyclerView.setLayoutManager(gridLayoutManager);

        new Thread(new Runnable() {
            @Override
            public void run() {
                initExpressionData();
            }
        }).start();

        adapter = new ExpressionListAdapter(expressionList,false);
        recyclerView.setAdapter(adapter);

        if (expressionList.size() == 0){
            adapter.setNewData(null);
            adapter.setEmptyView(notDataView);
        }

        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

                currentPosition = position;
                Expression expression = expressionList.get(position);
                expressionDialog.setImageData(expression);
                expressionDialog.show();

            }
        });
    }


    /**
     * 初始化表情包数据
     */
    private void initExpressionData(){
        Bundle bundle = getArguments();
        assert bundle != null;
        try {
            String jsonString = bundle.getString("data");
            tabName = bundle.getString("name");
            if (Objects.equals(jsonString, "")){
                expressionList = new ArrayList<>();
            }else {
                ObjectMapper mapper = new ObjectMapper();
                JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, Expression.class);
                expressionList = mapper.readValue(jsonString, javaType);
                ALog.d("list",expressionList.size());
                if (getActivity()!=null){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.setNewData(expressionList);
                        }
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void initView(){
        expressionDialog  = new ExpImageDialog.Builder(Objects.requireNonNull(getActivity()))
                .setContext(getActivity(),this)
                .build();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshUI(EventMessage eventBusMessage) {
        if (Objects.equals(eventBusMessage.getType(), EventMessage.LOCAL_DESCRIPTION_SAVE)) {
            ALog.d("怎么回事？");
            if (Objects.equals(eventBusMessage.getMessage2(), tabName)){
                ALog.d("更新首页布局");
                currentPosition= Integer.parseInt(eventBusMessage.getMessage3());
                expressionList.get(currentPosition).setDesStatus(1);
                expressionList.get(currentPosition).setDescription(eventBusMessage.getMessage());
            }
        }
    }

}

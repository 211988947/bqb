package com.ihewro.android_expression_package.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.ALog;
import com.chad.library.adapter.base.animation.BaseAnimation;
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback;
import com.chad.library.adapter.base.listener.OnItemDragListener;
import com.ihewro.android_expression_package.GlobalConfig;
import com.ihewro.android_expression_package.R;
import com.ihewro.android_expression_package.adapter.ExpMyRecyclerViewAdapter;
import com.ihewro.android_expression_package.bean.EventMessage;
import com.ihewro.android_expression_package.bean.ExpressionFolder;
import com.ihewro.android_expression_package.callback.UpdateDatabaseListener;
import com.ihewro.android_expression_package.task.UpdateDatabaseTask;
import com.ihewro.android_expression_package.util.CheckPermissionUtils;
import com.ihewro.android_expression_package.util.DataUtil;
import com.ihewro.android_expression_package.util.DateUtil;
import com.ihewro.android_expression_package.util.UIUtil;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.LitePal;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import pub.devrel.easypermissions.EasyPermissions;

public class MyActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks{

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    View notDataView;

    //适配器
    private ExpMyRecyclerViewAdapter adapter;

    private List<ExpressionFolder> expressionFolderList = new ArrayList<>();

    OnItemDragListener onItemDragListener = new OnItemDragListener() {
        int start = 0;
        float end = 0;
        @Override
        public void onItemDragStart(RecyclerView.ViewHolder viewHolder, int pos){
            start = pos;
            ALog.d("开始" + pos);
        }
        @Override
        public void onItemDragMoving(RecyclerView.ViewHolder source, int from, RecyclerView.ViewHolder target, int to) {
            ALog.d("开始" + from + " || 目标" + to);

        }
        @Override
        public void onItemDragEnd(RecyclerView.ViewHolder viewHolder, int pos) {
            //修改表情中表情包权值，移动的表情包权值 = 移动后的位置
            ALog.d("结束" + pos);
            end = pos;
            expressionFolderList.get((int) end).setOrderValue(end + 1.5);
            expressionFolderList.get((int) end).save();
            EventBus.getDefault().post(new EventMessage(EventMessage.MAIN_DATABASE));


        }
    };

    public static void actionStart(Activity activity){
        Intent intent = new Intent(activity,MyActivity.class);
        activity.startActivityForResult(intent,2);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        initView();

        initListener();

        refreshLayout.autoRefresh();
    }


    private void initView() {

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        notDataView = getLayoutInflater().inflate(R.layout.item_empty_view, (ViewGroup) recyclerView.getParent(), false);
        refreshLayout.setEnableLoadMore(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(UIUtil.getContext()));
        adapter = new ExpMyRecyclerViewAdapter(expressionFolderList,this);
        ItemDragAndSwipeCallback itemDragAndSwipeCallback = new ItemDragAndSwipeCallback(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemDragAndSwipeCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // 开启拖拽
        adapter.enableDragItem(itemTouchHelper, R.id.item_view, true);
        adapter.setOnItemDragListener(onItemDragListener);

        recyclerView.setAdapter(adapter);
    }




    /**
     * 读取数据库的信息，获取本地的图片信息
     */
    private void initData() {
        //查询到所有的表情包目录，但是有的表情包目录status可能是-1，即无效表情包
        new Thread(new Runnable() {
            @Override
            public void run() {
                expressionFolderList = LitePal.order("ordervalue").find(ExpressionFolder.class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ALog.d("listSize",expressionFolderList.size());
                        if (expressionFolderList.size() == 0){
                            adapter.setNewData(null);
                            adapter.setEmptyView(notDataView);
                        }else {
                            adapter.setNewData(expressionFolderList);
                        }
                        refreshLayout.finishRefresh();
                        refreshLayout.setEnableRefresh(false);
                    }
                });
            }
        }).start();


    }

    /**
     * 监听事件
     */
    private void initListener() {
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                initData();
            }
        });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_my, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.re_update){
            //重新同步数据库
            String[] notPermission = CheckPermissionUtils.checkPermission(UIUtil.getContext());
            if (notPermission.length != 0) {//需要的权限没有全部被运行
                ActivityCompat.requestPermissions(this, notPermission, 100);
            }else {
                updateDatabase();
            }

        }else if (item.getItemId() == android.R.id.home) {
            finish();
        }else if (item.getItemId() == R.id.re_add){
            //新建表情文件夹
            new MaterialDialog.Builder(this)
                    .title("输入表情包名称")
                    .content("具有一点分类意义的名字哦，方便查找")
                    .inputType(InputType.TYPE_CLASS_TEXT)
                    .input("任意文字", "", new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(MaterialDialog dialog, CharSequence input) {
                            // Do something
                            List<ExpressionFolder> temExpFolderList = LitePal.where("name = ?",dialog.getInputEditText().getText().toString()).find(ExpressionFolder.class);
                            if (temExpFolderList.size()>0){
                                Toasty.error(MyActivity.this,"目录名称已存在，请更换",Toast.LENGTH_SHORT).show();
                            }else {
                                ExpressionFolder expressionFolder = new ExpressionFolder(1,0,dialog.getInputEditText().getText().toString(),null,null, DateUtil.getNowDateStr(),null,null,-1);
                                expressionFolder.save();
                                initData();
                            }
                            EventBus.getDefault().post(new EventMessage(EventMessage.DATABASE));
                        }
                    }).show();
        }
        return super.onOptionsItemSelected(item);
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void refreshUI(EventMessage eventBusMessage){
        if (Objects.equals(eventBusMessage.getType(), EventMessage.DATABASE)){
            ALog.d("更新首页布局");
            initData();
        }
    }

    private void updateDatabase(){
        new MaterialDialog.Builder(this)
                .title("操作通知")
                .content("同步数据可以解决两个问题:\n\n" +
                        "1. 表情显示的数目不正确\n" +
                        "2. 同步过程中自动为您识别表情文字，作为表情描述方便搜索")
                .positiveText("朕确定")
                .negativeText("我只是点着玩的，快关掉快关掉！")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        UpdateDatabaseTask task = new UpdateDatabaseTask(MyActivity.this,new UpdateDatabaseListener() {

                            private MaterialDialog updateLoadingDialog;

                            @Override
                            public void onFinished() {
                                updateLoadingDialog.setContent("终于同步完成");
                                updateLoadingDialog.dismiss();
                                Toasty.success(MyActivity.this,"同步完成", Toast.LENGTH_SHORT).show();
                                //更新RecyclerView 布局
                                initData();

                            }

                            @Override
                            public void onProgress(int progress,int max) {
                                if (max > 0){
                                    if (!updateLoadingDialog.isShowing()){
                                        updateLoadingDialog.setMaxProgress(max);
                                        updateLoadingDialog.show();
                                    }

                                    if (progress > 0){
                                        updateLoadingDialog.setProgress(progress);
                                    }

                                }
                            }

                            @Override
                            public void onStart() {
                                updateLoadingDialog = new MaterialDialog.Builder(MyActivity.this)
                                        .title("正在同步信息")
                                        .content("陛下，耐心等下……（同步过程）")
                                        .progress(false, 0, true)
                                        .build();

                            }
                        });
                        task.execute();
                    }
                })
                .show();

    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        //权限被申请成功
        Toast.makeText(UIUtil.getContext(), "权限申请成功，愉快使用表情宝宝吧😁", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // 权限被拒绝
        Toast.makeText(UIUtil.getContext(), "权限没有被通过，该软件运行过程中可能会闪退，请留意", Toast.LENGTH_SHORT).show();
    }

}

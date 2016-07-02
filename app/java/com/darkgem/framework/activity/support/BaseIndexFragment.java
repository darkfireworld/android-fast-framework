package com.darkgem.framework.activity.support;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.*;
import android.widget.*;

import java.util.*;

public abstract class BaseIndexFragment extends BaseFragment {
    //对比类
    Comparator<IndexItem> comparator = null;
    //字母列表
    String[] alphas = null;
    Map<String, Integer> alphaMap = null;
    //适配器
    BaseAdapter adapter = null;
    //ListView Item数据
    List<IndexItem> data = null;

    //视图模块
    ListView lv_main = null;

    public static int dp2px(Context context, float value) {
        final float scale = context.getResources().getDisplayMetrics().densityDpi;
        return (int) (value * (scale / 160) + 0.5f);
    }

    /**
     * sp转px.
     *
     * @param value the value
     * @return the int
     */
    public static int sp2px(Context context, float value) {
        Resources r;
        if (context == null) {
            r = Resources.getSystem();
        } else {
            r = context.getResources();
        }
        float spvalue = value * r.getDisplayMetrics().scaledDensity;
        return (int) (spvalue + 0.5f);
    }

    /**
     * 排序
     *
     * @param alphaMap 字母表
     * @param lhs      左边
     * @param rhs      右边
     * @return -1 lhs < rhs, 0 lhs = rhs , 1 lhs > rhs
     */
    protected int compare(Map<String, Integer> alphaMap, IndexItem lhs, IndexItem rhs) {
        Integer l = alphaMap.get(lhs.getAlpha());
        Integer r = alphaMap.get(rhs.getAlpha());
        //如果其中任意一方不能获取到数值，则不进行排序
        if (l == null || r == null) {
            return 0;
        }
        return l - r;
    }

    /**
     * BaseIndexFragment onCreate, 配置除View以外所有的部分
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化对比
        comparator = new Comparator<IndexItem>() {

            @Override
            public int compare(IndexItem lhs, IndexItem rhs) {
                return BaseIndexFragment.this.compare(alphaMap, lhs, rhs);
            }
        };
        //初始化字母表, 和快速排序辅助Map
        alphas = getAlphas();
        alphaMap = new HashMap<String, Integer>();
        for (int i = 0; i < alphas.length; ++i) {
            alphaMap.put(alphas[i], i);
        }
        //设置试图适配器
        adapter = new BaseAdapter() {
            @Override
            public int getViewTypeCount() {
                return BaseIndexFragment.this.getViewTypeCount();
            }

            @Override
            public int getItemViewType(int position) {
                return BaseIndexFragment.this.getItemViewType(position);
            }

            @Override
            public int getCount() {
                return BaseIndexFragment.this.getCount();
            }

            @Override
            public Object getItem(int position) {
                return data.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return BaseIndexFragment.this.getView(position, convertView, parent);
            }
        };
        //数据, 默认size为空
        data = new LinkedList<IndexItem>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FrameLayout fl_container = null;
        IndexAlphaView ia_left = null;
        //初始化各个View, 进行基础配置
        {
            fl_container = new FrameLayout(getActivity());
            fl_container.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            //中间listView
            lv_main = new ListView(getActivity());
            lv_main.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            //关闭快速滑动指示器
            lv_main.setDivider(null);
            lv_main.setVerticalScrollBarEnabled(false);
            lv_main.setHorizontalScrollBarEnabled(false);
            lv_main.setSelector(android.R.color.transparent);
            lv_main.setCacheColorHint(Color.TRANSPARENT);
            lv_main.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    //隐藏键盘
                    hideSoftKeyboard();
                    return false;
                }
            });

            //中间Tip
            TextView tv_mid = new TextView(getActivity());
            tv_mid.setLayoutParams(new FrameLayout.LayoutParams(dp2px(getActivity(), 60), dp2px(getActivity(), 60), Gravity.CENTER));
            tv_mid.setGravity(Gravity.CENTER);
            tv_mid.setTextColor(0xffffffff);
            tv_mid.setTextSize(sp2px(getActivity(), 12));
            tv_mid.setVisibility(View.GONE);
            {
                // 外部矩形弧度
                int _8 = dp2px(getActivity(), 8);
                float[] outerRadii = new float[]{_8, _8, _8, _8, _8, _8, _8, _8};
                RoundRectShape roundRect = new RoundRectShape(outerRadii, null, null);
                ShapeDrawable bgDrawable = new ShapeDrawable(roundRect);
                bgDrawable.getPaint().setColor(0x5f333333);
                tv_mid.setBackgroundDrawable(bgDrawable);
            }
            //侧边字母索引
            ia_left = new IndexAlphaView(getActivity(), alphas, tv_mid);
            ia_left.setLayoutParams(new FrameLayout.LayoutParams(dp2px(getActivity(), 25), ViewGroup.LayoutParams.MATCH_PARENT, Gravity.RIGHT));

            fl_container.addView(lv_main);
            fl_container.addView(tv_mid);
            fl_container.addView(ia_left);
        }
        //设置
        {

            final List<View> heads = getHeadViews(inflater);
            View foot = getFootView(inflater);
            if (heads != null) {
                for (int i = 0; i < heads.size(); ++i) {
                    lv_main.addHeaderView(heads.get(i));
                }
            }
            if (foot != null) {
                lv_main.addFooterView(foot);
            }
            //配置Adapter
            lv_main.setAdapter(adapter);
            //配置字母索引
            ia_left.setOnTouchingIndexChangedListener(new IndexAlphaView.OnTouchingIndexChangedListener() {
                @Override
                public void onTouchingIndexChanged(String s) {
                    int pos = -1;
                    for (int i = 0; i < data.size(); ++i) {
                        if (data.get(i).getAlpha().equals(s)) {
                            pos = i;
                            break;
                        }
                    }
                    if (pos != -1) {
                        int shift = 0;
                        if (heads != null) {
                            shift = heads.size();
                        }
                        lv_main.setSelection(shift + pos);
                    }
                }
            });
        }
        return fl_container;
    }

    /**
     * 获取视图类型的数量, 默认为1
     */
    protected int getViewTypeCount() {
        return 1;
    }

    /**
     * 如果数据变化, 则一定要调用这个方法, 并且重新排序给定的数据
     */
    public void setData(List<IndexItem> data) {
        this.data = data;
        //排序
        Collections.sort(data, comparator);
        adapter.notifyDataSetChanged();
    }

    /**
     * 首部View
     */
    protected List<View> getHeadViews(LayoutInflater inflater) {
        return null;
    }

    /**
     * 尾部View
     */
    protected View getFootView(LayoutInflater inflater) {
        return null;
    }

    /**
     * 获取字母索引
     */
    abstract protected String[] getAlphas();

    /**
     * 获取视图
     */
    private View getView(int position, View convertView, ViewGroup parent) {
        return getView(data, position, convertView, parent);
    }

    /**
     * 获得data的长度
     *
     * @return
     */
    private int getCount() {
        return getCount(data);
    }

    /**
     * 获得数据的长度
     *
     * @param data
     * @return
     */
    protected int getCount(List<IndexItem> data) {
        return data.size();
    }

    /**
     * 获知指定位置, 具体视图为什么类型
     */
    private int getItemViewType(int position) {
        return getItemViewType(data, position);
    }

    /**
     * 获知指定位置, 具体视图为什么类型
     */
    protected int getItemViewType(List<IndexItem> data, int position) {
        return 0;
    }

    /**
     * getView 每个Item的View
     */
    abstract protected View getView(List<IndexItem> data, int position, View convertView, ViewGroup parent);

    /**
     * ListView Item 点击处理
     */
    protected void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        lv_main.setOnItemClickListener(listener);
    }

    /**
     * ListView Item 长按处理
     */
    protected void setOnLongClickListener(AdapterView.OnItemLongClickListener listener) {
        lv_main.setOnItemLongClickListener(listener);
    }

    /**
     * ListView 数据项
     */
    public interface IndexItem {
        @Nullable
        String getImage();

        String getName();

        String getAlpha();
    }

    /**
     * 右侧快速滚动栏
     */
    public static class IndexAlphaView extends View {
        // 26个字母
        private String[] alphas;

        // 触摸事件
        private OnTouchingIndexChangedListener onTouchingIndexChangedListener;
        private int choose = -1;// 选中
        private Paint paint = new Paint();

        private TextView mTextDialog;

        public IndexAlphaView(Context context, String[] alphas, TextView textDialog) {
            super(context);
            this.alphas = alphas;
            this.mTextDialog = textDialog;
        }

        /**
         * 重写这个方法
         */
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            // 获取焦点改变背景颜色.
            int height = getHeight();// 获取对应高度
            int width = getWidth(); // 获取对应宽度
            int singleHeight = height / alphas.length;// 获取每一个字母的高度

            for (int i = 0; i < alphas.length; i++) {
                paint.setColor(Color.GRAY);
                paint.setTypeface(Typeface.DEFAULT_BOLD);
                paint.setAntiAlias(true);
                paint.setTextSize(sp2px(getContext(), 12));
                // 选中的状态
                if (i == choose) {
                    paint.setColor(Color.parseColor("#3399ff"));
                    paint.setFakeBoldText(true);
                }
                // x坐标等于中间-字符串宽度的一半.
                float xPos = width / 2 - paint.measureText(alphas[i]) / 2;
                float yPos = singleHeight * i + singleHeight;
                canvas.drawText(alphas[i], xPos, yPos, paint);
                paint.reset();// 重置画笔
            }

        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            final int action = event.getAction();
            final float y = event.getY();// 点击y坐标
            final int oldChoose = choose;
            final OnTouchingIndexChangedListener listener = onTouchingIndexChangedListener;
            final int c = (int) (y / getHeight() * alphas.length);// 点击y坐标所占总高度的比例*b数组的长度就等于点击b中的个数.

            switch (action) {
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    setBackgroundDrawable(new ColorDrawable(0x00000000));
                    choose = -1;//
                    invalidate();
                    if (mTextDialog != null) {
                        mTextDialog.setVisibility(View.INVISIBLE);
                    }
                    break;

                default:
                    //设置右侧字母列表[A,B,C,D,E....]的背景颜色
                    //setBackgroundResource(R.drawable.v2_sortlistview_sidebar_background);
                    if (oldChoose != c) {
                        if (c >= 0 && c < alphas.length) {
                            if (listener != null) {
                                listener.onTouchingIndexChanged(alphas[c]);
                            }
                            if (mTextDialog != null) {
                                mTextDialog.setText(alphas[c]);
                                mTextDialog.setVisibility(View.VISIBLE);
                            }

                            choose = c;
                            invalidate();
                        }
                    }
                    break;
            }
            return true;
        }

        /**
         * 向外公开的方法
         */
        public void setOnTouchingIndexChangedListener(
                OnTouchingIndexChangedListener onTouchingIndexChangedListener) {
            this.onTouchingIndexChangedListener = onTouchingIndexChangedListener;
        }

        /**
         * 接口
         */
        public interface OnTouchingIndexChangedListener {
            void onTouchingIndexChanged(String s);
        }
    }
}

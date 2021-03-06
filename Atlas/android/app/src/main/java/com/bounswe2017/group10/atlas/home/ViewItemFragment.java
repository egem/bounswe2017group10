package com.bounswe2017.group10.atlas.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bounswe2017.group10.atlas.R;
import com.bounswe2017.group10.atlas.adapter.CommentAdapter;
import com.bounswe2017.group10.atlas.adapter.CommentRow;
import com.bounswe2017.group10.atlas.adapter.FeedRow;
import com.bounswe2017.group10.atlas.adapter.ImageListAdapter;
import com.bounswe2017.group10.atlas.adapter.ImageRow;
import com.bounswe2017.group10.atlas.adapter.ListItemsAdapter;
import com.bounswe2017.group10.atlas.adapter.NoScrollListView;
import com.bounswe2017.group10.atlas.adapter.TagListAdapter;
import com.bounswe2017.group10.atlas.httpbody.Comment;
import com.bounswe2017.group10.atlas.httpbody.CultureItem;
import com.bounswe2017.group10.atlas.httpbody.Image;
import com.bounswe2017.group10.atlas.httpbody.PostCommentRequest;
import com.bounswe2017.group10.atlas.httpbody.Tag;
import com.bounswe2017.group10.atlas.remote.APIUtils;
import com.bounswe2017.group10.atlas.response.OnGetItemsResponse;
import com.bounswe2017.group10.atlas.response.OnPostCommentResponse;
import com.bounswe2017.group10.atlas.util.Constants;
import com.bounswe2017.group10.atlas.util.Utils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.bounswe2017.group10.atlas.util.Utils.getSharedPref;

public class ViewItemFragment extends Fragment {

    private CultureItem mItem;
    private CommentAdapter mAdapter;
    private final ArrayList<CommentRow> mRowList = new ArrayList<>();
    private final ArrayList<Comment> mCommentList = new ArrayList<>();
    boolean isFirstTimeClickToEdit = true;
    private Activity mActivity;

    private boolean hasRequestedRecommendations = false;
    private final ArrayList<CultureItem> mRecommendedItemList = new ArrayList<>();
    private final ArrayList<FeedRow> mRecommendedRowList = new ArrayList<>();
    private ListItemsAdapter mRecommendAdapter;
    private OnGetItemsResponse.GetItemCallback mGetItemCallback;
    private ArrayList<ListItemsFragment.AfterItemClickedListener> mListenerList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_item, container, false);
        mItem = getArguments().getParcelable(Constants.CULTURE_ITEM);
        mActivity = getActivity();

        RecyclerView tagRecyclerView = view.findViewById(R.id.tag_recyclerview);
        setTags(tagRecyclerView, mItem);

        TextView ewTitle = view.findViewById(R.id.itemTitle);
        TextView ewDescription = view.findViewById(R.id.itemDesc);
        TextView ewLocation = view.findViewById(R.id.location_textview_detail);
        TextView ewDate = view.findViewById(R.id.year_textview_detail);
        TextView ewOwner = view.findViewById(R.id.owner_textview_detail);
        TextView ewFavCount = view.findViewById(R.id.favorite_textview_detail);
        setText(ewTitle, ewDescription, ewLocation, ewDate, ewOwner, ewFavCount, mItem);

        RecyclerView gallery = view.findViewById(R.id.image_gallery);
        setImages(gallery, mItem);

        NoScrollListView listView = view.findViewById(R.id.comment_listview);

        mAdapter = new CommentAdapter(getContext(), mRowList);
        listView.setAdapter(mAdapter);

        for(Comment comment : mItem.getCommentList()) {
            mCommentList.add(0,comment);
            mRowList.add(0,comment.toCommentRow());
        }

        EditText commentEdit = view.findViewById(R.id.comment_edit);
        Button sendButton = view.findViewById(R.id.comment_send);

        commentEdit.setOnClickListener((View btnView) -> {
            if(isFirstTimeClickToEdit)  {
                commentEdit.setText("");
                isFirstTimeClickToEdit = false;
            }
        });
        sendButton.setOnClickListener((View btnView) -> {
            String text = commentEdit.getText().toString();
            commentEdit.setText("");
            String authStr = Utils.getSharedPref(mActivity).getString(Constants.AUTH_STR, Constants.NO_AUTH_STR);
            OnPostCommentResponse respHandler = new OnPostCommentResponse(mActivity, mCommentList, mRowList, mAdapter);
            Comment pack = new Comment();
            pack.setText(text);
            PostCommentRequest requestBody = new PostCommentRequest();
            requestBody.setComment(pack);
            APIUtils.serverAPI().postComment(authStr,mItem.getId(), requestBody).enqueue(respHandler);
        });

        if (!hasRequestedRecommendations) {
            hasRequestedRecommendations = true;

            LinearLayout mRecommendationPBarLayout = view.findViewById(R.id.recommendations_pbar_layout);
            mGetItemCallback = (List<CultureItem> itemList) -> {
                for (CultureItem item : itemList) {
                    mRecommendedItemList.add(item);
                    mRecommendedRowList.add(item.toFeedRow());
                }
                mRecommendAdapter.notifyDataSetChanged();
                mRecommendationPBarLayout.setVisibility(View.GONE);
            };
            requestRecommendedItems(getActivity(),mGetItemCallback);

            mRecommendAdapter = new ListItemsAdapter(getActivity(), mRecommendedRowList, (List<FeedRow> rowList, int position) -> {
                // put item to bundle
                Bundle itemBundle = new Bundle();
                itemBundle.putParcelable(Constants.CULTURE_ITEM, mRecommendedItemList.get(position));
                // put bundle to fragment
                ViewItemFragment viewItemFragment = new ViewItemFragment();
                viewItemFragment.setArguments(itemBundle);
                // go to fragment
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.home_container, viewItemFragment)
                        .addToBackStack(null)
                        .commit();
                for (ListItemsFragment.AfterItemClickedListener listener : mListenerList) {
                    listener.afterClicked();
                }
            });
        } else {
            LinearLayout mRecommendationPBarLayout = view.findViewById(R.id.recommendations_pbar_layout);
            mRecommendationPBarLayout.setVisibility(View.GONE);
        }
        RecyclerView recyclerView = view.findViewById(R.id.recommendations_recyclerview);
        SnapHelper helper = new LinearSnapHelper();
        helper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(mRecommendAdapter);

        return view;
    }

    /**
     * Set the buttons in the action bar. Show edit/delete buttons only if the
     * current user is the creator of the item.
     *
     * @param menu Menu object.
     * @param inflater MenuInflater object.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.action_view_item, menu);

        long currentUserId = Utils.getSharedPref(mActivity).getLong(Constants.USER_ID, -1);
        if (currentUserId != mItem.getUser()) {
            menu.findItem(R.id.action_edit).setVisible(false);
            menu.findItem(R.id.action_delete).setVisible(false);
        }

        if (mItem.isFavorite()) {
            menu.findItem(R.id.action_favorite).setIcon(R.drawable.ic_favorite_black_24dp);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }


    /**
     * Select actions to perform when one of the buttons in the action bar is clicked.
     *
     * @param item MenuItem object (button).
     * @return True if we can handle the request; delegate to superclass if not.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favorite:
                if(mItem.isFavorite())
                    unfavoriteItem();

                else if(!(mItem.isFavorite()))
                    favoriteItem();

                else
                    Utils.showToast(mActivity, getString(R.string.error_favorite));
                return true;
            case R.id.action_edit:
                Intent intent = new Intent(mActivity, CreateItemActivity.class);
                intent.putExtra(Constants.CULTURE_ITEM, mItem);
                intent.putExtra(Constants.CREATE_STRATEGY, Constants.EDIT);
                startActivity(intent);
                return true;
            case R.id.action_delete:
                deleteItem();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Delete the current item and exit the current Fragment.
     */
    private void deleteItem() {
        String authStr = Utils.getSharedPref(mActivity).getString(Constants.AUTH_STR, Constants.NO_AUTH_STR);
        APIUtils.serverAPI().deleteItem(authStr, mItem.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Utils.showToast(mActivity, getString(R.string.successful_item_delete));
                    mActivity.onBackPressed();
                } else {
                    Utils.showToast(mActivity, getString(R.string.unable_to_delete));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Utils.showToast(mActivity, getString(R.string.connection_failure));
            }
        });
    }

    private void favoriteItem() {
        String authStr = Utils.getSharedPref(mActivity).getString(Constants.AUTH_STR, Constants.NO_AUTH_STR);
        APIUtils.serverAPI().favoriteItem(authStr, mItem.getId()).enqueue(new Callback<CultureItem>() {
            @Override
            public void onResponse(Call<CultureItem> call, Response<CultureItem> response) {
                if (response.isSuccessful()) {
                    //
                    Utils.showToast(mActivity, getString(R.string.succesful_favorite));
                    //menu.findItem(R.id.action_favorite).setIcon(R.drawable.ic_favorite_black_24dp);
                    mItem.setFavorite(true);
                    mActivity.invalidateOptionsMenu();
                } else {
                    Utils.showToast(mActivity, getString(R.string.error_favorite));
                }
            }

            @Override
            public void onFailure(Call<CultureItem> call, Throwable t) {
                Utils.showToast(mActivity, getString(R.string.connection_failure));
            }
        });
    }

    private void unfavoriteItem() {
        String authStr = Utils.getSharedPref(mActivity).getString(Constants.AUTH_STR, Constants.NO_AUTH_STR);
        APIUtils.serverAPI().unfavoriteItem(authStr, mItem.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    //
                    Utils.showToast(mActivity, getString(R.string.succesful_unfavorite));
                    //menu.findItem(R.id.action_favorite).setIcon(R.drawable.ic_favorite_black_24dp);
                    mItem.setFavorite(false);
                    mActivity.invalidateOptionsMenu();
                } else {
                    Utils.showToast(mActivity, getString(R.string.error_favorite));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Utils.showToast(mActivity, getString(R.string.connection_failure));
            }
        });
    }

    /**
     * Set the view to show the tags contained in the given CultureItem object.
     *
     * @param tagRecyclerView RecyclerView that is responsible for showing tags.
     * @param item CultureItem object.
     */
    private void setTags(RecyclerView tagRecyclerView, CultureItem item) {
        List<Tag> tagList = item.getTagList();
        TagListAdapter tagAdapter = new TagListAdapter(mActivity, tagList, null);
        tagRecyclerView.setAdapter(tagAdapter);
    }

    /**
     * Set the view to show item's title and description.
     *
     * @param twTitle TextView responsible for showing the title of the CultureItem.
     * @param twDescription TextView responsible for showing the description of the CultureItem.
     * @param item CultureItem object.
     */
    private void setText(TextView twTitle, TextView twDescription, TextView twLocation,
                         TextView twDate, TextView twOwner, TextView twFavCount, CultureItem item) {
        twTitle.setText(item.getTitle());
        twDescription.setText(item.getDescription());
        twLocation.setText(item.getPlaceName());
        if(item.getStartYear() != null && item.getEndYear() != null) {
            String startYearStr = Utils.yearString(item.getStartYear());
            String endYearStr = Utils.yearString(item.getEndYear());
            twDate.setText(getString(R.string.year_string, startYearStr, endYearStr));
        }
        twOwner.setText(item.getUserInfo().getUsername());
        twFavCount.setText(item.getFavoriteCount());

    }

    /**
     * Set the view to show the media items of the given CultureItem.
     *
     * @param gallery Gallery object responsible for showing all the media items of a given CultureItem.
     * @param item CultureItem object.
     */
    private void setImages(RecyclerView gallery, CultureItem item) {
        ArrayList<ImageRow> imageRowList = new ArrayList<>();
        for (Image img : item.getImageList()) {
            ImageRow row = new ImageRow();
            row.setUri(Uri.parse(img.getUrl()));
            imageRowList.add(row);
        }
        ImageListAdapter adapter = new ImageListAdapter(mActivity, imageRowList, new ImageListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(List<ImageRow> rowList, int position) {
                Intent intent = new Intent(getActivity(), FullScreenImageActivity.class);
                intent.putExtra(Constants.IMAGE_LIST, mItem.getImageList());
                startActivity(intent);
            }
        });
        SnapHelper helper = new LinearSnapHelper();
        helper.attachToRecyclerView(gallery);
        gallery.setAdapter(adapter);
    }

    public void requestRecommendedItems(Context context, OnGetItemsResponse.GetItemCallback getItemCallback) {
        String authStr = getSharedPref(context).getString(Constants.AUTH_STR, Constants.NO_AUTH_STR);
        OnGetItemsResponse respHandler = new OnGetItemsResponse(context, getItemCallback);
        APIUtils.serverAPI().getRecommendedItems(authStr, mItem.getId(),Constants.RECOMMENDATION_AMOUNT).enqueue(respHandler);
    }

}

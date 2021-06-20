package com.dev.umieplayer.fragments;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dev.umieplayer.MainActivity;
import com.dev.umieplayer.R;
import com.dev.umieplayer.interfaces.FragmentOpener;
import com.dev.umieplayer.interfaces.HomePageUpdate;
import com.dev.umieplayer.objects.MusicItem;
import com.dev.umieplayer.objects.PlayList;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class LibraryFragment extends Fragment implements FragmentOpener {

    private ViewPager pager;
    private PagerAdapter pagerAdapter;
    private AllListsFragment plPage;
    private AllListsFragment auPage;
    private AllListsFragment alPage;

    private FragmentOpener fragmentOpener;

    public LibraryFragment() {
        // Required empty public constructor
    }

    public static LibraryFragment newInstance(){
        return new LibraryFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_libary, container, false);
        fragmentOpener = this;
        plPage = new AllListsFragment(0, new FragmentOpener() {
            @Override
            public void openPlayList(PlayList list, int index,String type) {
                FragmentManager fm = getFragmentManager();
                PlayListFragment playListFragment = new PlayListFragment(list, index, new HomePageUpdate() {
                    @Override
                    public void updatePlayLists() {
//                            pagerAdapter.notifyDataSetChanged();
//                            pager.invalidate();
                        ((MainActivity)getActivity()).updateAdapter();
                    }
                },"all");
                fm.beginTransaction().setCustomAnimations(R.anim.slide_right_anim,R.anim.slide_out).replace(R.id.listLayoutAll, playListFragment).commit();
            }

            @Override
            public void openCatalog(PlayList list, int index,String type) {
                FragmentManager fm = getFragmentManager();
                CatalogFragment cFragment = new CatalogFragment(list,type,index);
                fm.beginTransaction().setCustomAnimations(R.anim.slide_right_anim,R.anim.slide_out).replace(R.id.listLayoutAll, cFragment).commit();
            }

            @Override
            public void openAllLists() {

            }

            @Override
            public void openSelector(Bitmap image, String name, ArrayList<MusicItem> selected) {

            }

            @Override
            public void openCreator(String s, Bitmap default_music_image, ArrayList<MusicItem> o) {
                fragmentOpener.openCreator(s,default_music_image,o);
            }
        });
        auPage = new AllListsFragment(1, new FragmentOpener() {
            @Override
            public void openPlayList(PlayList list, int index,String type) {
                FragmentManager fm = getFragmentManager();
                PlayListFragment playListFragment = new PlayListFragment(list, index, new HomePageUpdate() {
                    @Override
                    public void updatePlayLists() {
//                            pagerAdapter.notifyDataSetChanged();
//                            pager.invalidate();
                        ((MainActivity)getActivity()).updateAdapter();
                    }
                },"all");
                fm.beginTransaction().setCustomAnimations(R.anim.slide_right_anim,R.anim.slide_out).replace(R.id.listLayoutAll, playListFragment).commit();
            }

            @Override
            public void openCatalog(PlayList list, int index,String type) {
                FragmentManager fm = getFragmentManager();
                CatalogFragment cFragment = new CatalogFragment(list,type,index);
                fm.beginTransaction().setCustomAnimations(R.anim.slide_right_anim,R.anim.slide_out).replace(R.id.listLayoutAll, cFragment).commit();
            }

            @Override
            public void openAllLists() {

            }

            @Override
            public void openSelector(Bitmap image, String name, ArrayList<MusicItem> selected) {

            }

            @Override
            public void openCreator(String s, Bitmap default_music_image, ArrayList<MusicItem> o) {
            }
        });
        alPage = new AllListsFragment(2, new FragmentOpener() {
            @Override
            public void openPlayList(PlayList list, int index,String type) {
                FragmentManager fm = getFragmentManager();
                PlayListFragment playListFragment = new PlayListFragment(list, index, new HomePageUpdate() {
                    @Override
                    public void updatePlayLists() {
//                            pagerAdapter.notifyDataSetChanged();
//                            pager.invalidate();
                        ((MainActivity)getActivity()).updateAdapter();
                    }
                },"all");
                fm.beginTransaction().setCustomAnimations(R.anim.slide_right_anim,R.anim.slide_out).replace(R.id.listLayoutAll, playListFragment).commit();
            }

            @Override
            public void openCatalog(PlayList list, int index,String type) {
                FragmentManager fm = getFragmentManager();
                CatalogFragment cFragment = new CatalogFragment(list,type,index);
                fm.beginTransaction().setCustomAnimations(R.anim.slide_right_anim,R.anim.slide_out).replace(R.id.listLayoutAll, cFragment).commit();
            }

            @Override
            public void openAllLists() {

            }

            @Override
            public void openSelector(Bitmap image, String name, ArrayList<MusicItem> selected) {

            }

            @Override
            public void openCreator(String s, Bitmap default_music_image, ArrayList<MusicItem> o) {
            }
        });
        pager = v.findViewById(R.id.pager);
        pagerAdapter = new MyFragmentPagerAdapter(getActivity().getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        return v;
    }

    public void updateLists() {
        plPage.updatePl();auPage.updatePl();alPage.updatePl();
        pagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void openPlayList(PlayList list, int index,String type) {

    }

    @Override
    public void openCatalog(PlayList list, int index,String type) {

    }

    @Override
    public void openAllLists() {

    }

    @Override
    public void openSelector(Bitmap image, String name, ArrayList<MusicItem> selected) {

    }

    @Override
    public void openCreator(String s, Bitmap default_music_image, ArrayList<MusicItem> o) {
        FragmentManager fm = getFragmentManager();
        PlListCreatorFragment plFragment = new PlListCreatorFragment(s,default_music_image,o, new FragmentOpener() {
            @Override
            public void openPlayList(PlayList list, int index,String type) {

            }

            @Override
            public void openCatalog(PlayList list, int index,String type) {

            }

            @Override
            public void openAllLists() {

            }

            @Override
            public void openSelector(Bitmap image, String name, ArrayList<MusicItem> selected) {
                FragmentManager fm = getFragmentManager();
                MusicChooserFragment plFragment = new MusicChooserFragment(image, name, selected, fragmentOpener);
                fm.beginTransaction().setCustomAnimations(R.anim.slide_right_anim,R.anim.slide_out).replace(R.id.listLayoutAll, plFragment).commit();
            }

            @Override
            public void openCreator(String s, Bitmap default_music_image, ArrayList<MusicItem> o) {

            }
        });
        fm.beginTransaction().setCustomAnimations(R.anim.slide_right_anim,R.anim.slide_out).replace(R.id.listLayoutAll,plFragment).commit();
    }

    public void closePl() {
        if(getFragmentManager().findFragmentById(R.id.listLayoutAll)!=null) {
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().setCustomAnimations(R.anim.slide_right_anim,R.anim.slide_out).remove(getFragmentManager().findFragmentById(R.id.listLayoutAll)).commit();
        }
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Play lists";
                case 1:
                    return "Artists";
                default:
                    return "Albums";
            }
        }

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0: return plPage;
                case 1: return auPage;
                case 2:return alPage;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

    }
}

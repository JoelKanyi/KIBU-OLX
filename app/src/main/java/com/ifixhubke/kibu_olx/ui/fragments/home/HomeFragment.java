package com.ifixhubke.kibu_olx.ui.fragments.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.ifixhubke.kibu_olx.R;
import com.ifixhubke.kibu_olx.adapters.AllItemsAdapter;
import com.ifixhubke.kibu_olx.data.Item;
import com.ifixhubke.kibu_olx.databinding.FragmentHomeBinding;
import com.ifixhubke.kibu_olx.others.ItemClickListener;
import com.mancj.materialsearchbar.MaterialSearchBar;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import timber.log.Timber;

public class HomeFragment extends Fragment implements ItemClickListener, MaterialSearchBar.OnSearchActionListener,
        Toolbar.OnMenuItemClickListener, androidx.appcompat.widget.PopupMenu.OnMenuItemClickListener {

    FragmentHomeBinding binding;
    private DatabaseReference databaseReference;
    ArrayList<Item> itemsList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.d("onViewCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        Timber.d("onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        Timber.d("onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Timber.d("onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.d("onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Timber.d("onDetach");
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        Timber.d("onCreateView");

        databaseReference = FirebaseDatabase.getInstance().getReference();


        binding.toolbar.setOnMenuItemClickListener(this);
        binding.searchBar.setOnSearchActionListener(this);
        binding.searchBar.inflateMenu(R.menu.main_menu);
        binding.searchBar.getMenu().setOnMenuItemClickListener(this);

        binding.floatingActionButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_homeFragment2_to_sellFragmentOne));

        binding.searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                search(editable.toString());
            }
        });

        fetchItems();

        return view;
    }


    public void search(String itemName) {
        ArrayList<Item> filterItemsList = new ArrayList<>();

        for (Item item : itemsList) {
            if (item.getItemName().toLowerCase().contains(itemName.toLowerCase())) {
                filterItemsList.add(item);
            }
        }
        initializeRecycler(filterItemsList);
    }

    private void fetchItems() {
        databaseReference = FirebaseDatabase.getInstance().getReference("all_items");
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemsList.clear();
                if (snapshot.exists()) {
                    binding.imageView2.setVisibility(View.INVISIBLE);
                    binding.textView.setVisibility(View.INVISIBLE);

                    for (DataSnapshot i : snapshot.getChildren()) {
                       // Timber.d(i.toString());
                        Item item = i.getValue(Item.class);
                        itemsList.add(item);
                        //to reverse the list coz firebase sorts data in ascending order
                        Collections.reverse(itemsList);
                        binding.shimmerFrameLayout.setVisibility(View.INVISIBLE);
                        binding.allItemsRecyclerview.setVisibility(View.VISIBLE);
                    }
                    initializeRecycler(itemsList);
                } else {
                    binding.imageView2.setVisibility(View.VISIBLE);
                    binding.textView.setVisibility(View.VISIBLE);
                    binding.shimmerFrameLayout.setVisibility(View.INVISIBLE);
                    Timber.d("snapshot not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void initializeRecycler(ArrayList<Item> itemsList) {
        AllItemsAdapter adapter = new AllItemsAdapter(itemsList, this, getActivity());
        binding.allItemsRecyclerview.setAdapter(adapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.d("onPause");
        binding.shimmerFrameLayout.stopShimmer();
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume");
        binding.shimmerFrameLayout.startShimmer();
    }

    @Override
    public void itemClick(Item item, int position) {

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users")
                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("favorite_items");
            databaseReference.push().setValue(item).addOnSuccessListener(aVoid ->{

                Snackbar snackbar = Snackbar.make(requireView(), item.getItemName() + " added to favorites successfully", Snackbar.LENGTH_LONG);
                snackbar.setAction("Dismiss", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
                    }
                });
                snackbar.show();
            });
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.filter_menu) {
            Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment2_to_filterFragment);
            return true;
        } else if (item.getItemId() == R.id.shareMenu) {
            Toast.makeText(requireContext(), "Share Clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.aboutUsMenu) {
            Toast.makeText(requireContext(), "About Us Clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.inviteFriendMenu) {
            Toast.makeText(requireContext(), "Invite Friend Clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.rateUsMenu) {
            Toast.makeText(requireContext(), "Rate Us Clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.helpMenu) {
            Toast.makeText(requireContext(), "Help and Feedback Clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.logoutMenu) {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(requireContext(), "Logout Successfully", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment2_to_loginFragment);
            return true;
        } else
            return false;
    }


    @Override
    public void onSearchStateChanged(boolean enabled) {
    }

    @Override
    public void onSearchConfirmed(CharSequence text) {
    }

    @Override
    public void onButtonClicked(int buttonCode) {
    }
}

/*
* {
  "rules": {
    ".read": "now < 1616706000000",  // 2021-3-26
    ".write": "now < 1616706000000",  // 2021-3-26
  }
}*/
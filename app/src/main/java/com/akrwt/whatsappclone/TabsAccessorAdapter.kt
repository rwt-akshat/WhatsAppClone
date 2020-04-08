package com.akrwt.whatsappclone

import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class TabsAccessorAdapter(fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> {
                return ChatsFragment()
            }
            1 -> {
                return GroupFragment()
            }

            2 -> {
                return ContactsFragment()

            }
            3 -> {
                return RequestFragment()

            }
            else -> {
                return ChatsFragment()
            }
        }
    }

    override fun getCount(): Int {
        return 4
    }


    @Nullable
    @Override
    override fun getPageTitle(position: Int): CharSequence? {

        when (position) {
            0 -> {
                return "Chats"
            }
            1 -> {
                return "Groups"
            }
            2 -> {
                return "Contacts"
            }
            3 -> {
                return "Requests"
            }
        }

        return super.getPageTitle(position)
    }


}
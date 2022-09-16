package ___PACKAGE___

import android.net.Uri
import android.provider.ContactsContract
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.qmobile.qmobiledatasync.utils.BaseKotlinInputControl
import com.qmobile.qmobiledatasync.utils.KotlinInputControl
import com.qmobile.qmobileui.activity.mainactivity.ActivityResultController
import com.qmobile.qmobileui.activity.mainactivity.MainActivity
import com.qmobile.qmobileui.ui.SnackbarHelper
import com.qmobile.qmobileui.utils.PermissionChecker

@KotlinInputControl
class PhoneContact(private val view: View) : BaseKotlinInputControl {

    override val autocomplete: Boolean = false

    override fun getIconName(): String {
        return "call.xml"
    }
    
    private lateinit var outputCallback: (outputText: String) -> Unit

    private val contactPhoneNumberCallback: (contactUri: Uri?) -> Unit = { contactUri ->
        contactUri?.let {
            (view.context as MainActivity?)?.apply {
                contentResolver.query(contactUri, null, null, null, null)?.let { cursor ->
                    if (cursor.moveToFirst()) {
                        val contactIdIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                        val hasPhoneIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                        val contactId = cursor.getString(contactIdIndex)
                        val hasNumber = cursor.getString(hasPhoneIndex)
                        if (Integer.valueOf(hasNumber) == 1) {
                            contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                                null,
                                null
                            )?.let { numbersCursor ->
                                while (numbersCursor.moveToNext()) {
                                    val phoneNumberIndex =
                                        numbersCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                    val phoneNumber = numbersCursor.getString(phoneNumberIndex)
                                    outputCallback(phoneNumber)
                                    break
                                }
                            }
                        } else {
                            SnackbarHelper.show(this, "No phone number found in contact")
                            outputCallback("")
                        }
                    }
                }
            }
        }
    }

    override fun process(inputValue: Any?, outputCallback: (output: Any) -> Unit) {
        (view.context as PermissionChecker?)?.askPermission(
            permission = android.Manifest.permission.READ_CONTACTS,
            rationale = "Permission required to read contacts"
        ) { isGranted ->
            if (isGranted) {
                this.outputCallback = outputCallback
                (view.context as ActivityResultController?)?.launch(
                    type = ActivityResultContracts.PickContact(),
                    input = null,
                    callback = contactPhoneNumberCallback
                )
            }
        }
    }
}

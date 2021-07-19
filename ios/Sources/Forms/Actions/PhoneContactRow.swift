//
//  PhoneContactRow.swift
//  ___PACKAGENAME___
//
//  Created by ___FULLUSERNAME___ on ___DATE___
//  ___COPYRIGHT___
//

import UIKit
import ContactsUI

import Eureka
import QMobileUI

// name of the format
fileprivate let kPhoneContact = "phoneContact"

// Create an Eureka row for the format
final class PhoneContactRow: FieldRow<PhoneContactCell>, RowType {

    required public init(tag: String?) {
        super.init(tag: tag)
    }

}

// Create the associated row cell to display a button to pick contact
open class PhoneContactCell: PhoneCell, CNContactPickerDelegate {

    required public init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
    }

    required public init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }

    open override func setup() {
        super.setup()

        let pickButton = UIButton(primaryAction: UIAction(title: "", image: UIImage(systemName: "phone.circle"), identifier: nil, discoverabilityTitle: "phone pick", attributes: [], state: .on, handler: { action in
            self.pickContactProperty(CNContactPhoneNumbersKey)
        }))

        self.textField.rightView = pickButton
        self.textField.rightViewMode = .unlessEditing
    }

    fileprivate func pickContactProperty(_ contactProperty: String) {
        let contactPicker = CNContactPickerViewController()
        contactPicker.delegate = self
        contactPicker.displayedPropertyKeys = [contactProperty]
        contactPicker.predicateForEnablingContact = NSPredicate(format: "%K.@count > 0", contactProperty) // have property
        contactPicker.predicateForSelectionOfProperty = NSPredicate(format: "key == '\(contactProperty)'") // only selected property
        self.formViewController()?.present(contactPicker, animated: true) {
           // finish
        }
    }

    public func contactPicker(_ picker: CNContactPickerViewController, didSelect contactProperty: CNContactProperty) {
        if let phone = (contactProperty.value as? CNPhoneNumber)?.stringValue {
            self.row.value = phone
            self.textField.text = phone
        }
    }

    public func contactPickerDidCancel(_ picker: CNContactPickerViewController) {
        // cancelled
    }
}

@objc(PhoneContactRowService)
class PhoneContactRowService: NSObject, ApplicationService, ActionParameterCustomFormatRowBuilder {
    @objc static var instance: PhoneContactRowService = PhoneContactRowService()
    override init() {}
    func buildActionParameterCustomFormatRow(key: String, format: String, onRowEvent eventCallback: @escaping OnRowEventCallback) -> ActionParameterCustomFormatRowType? {
        if format == kPhoneContact {
            return PhoneContactRow(key).onRowEvent(eventCallback)
        }
        return nil
    }
}

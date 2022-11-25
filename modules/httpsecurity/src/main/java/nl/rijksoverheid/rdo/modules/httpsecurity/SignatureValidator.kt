package nl.rijksoverheid.rdo.modules.httpsecurity

import java.io.InputStream

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface SignatureValidator {
    @Throws(SignatureValidationException::class)
    fun validate(signature: ByteArray, content: InputStream)
}

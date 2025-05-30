/* 
/*******************************************************************************
 * Copyright (C) 2018 MINHAFP, Gobierno de Espa&ntilde;a
 * This program is licensed and may be used, modified and redistributed under the  terms
 * of the European Public License (EUPL), either version 1.1 or (at your option)
 * any later version as soon as they are approved by the European Commission.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and
 * more details.
 * You should have received a copy of the EUPL1.1 license
 * along with this program; if not, you may find it at
 * http:joinup.ec.europa.eu/software/page/eupl/licence-eupl
 ******************************************************************************/

/** 
 * <b>File:</b><p>es.gob.fire.persistence.service.ISignatureService.java.</p>
 * <b>Description:</b><p>Interface that provides communication with the operations of the persistence layer.</p>
  * <b>Project:</b><p>Application for signing documents of @firma suite systems</p>
 * <b>Date:</b><p>14/04/2020.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.0, 14/04/2020.
 */
package es.gob.fire.persistence.service;

import java.util.List;

import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import es.gob.fire.commons.utils.QueryEnum;
import es.gob.fire.persistence.dto.ApplicationDTO;
import es.gob.fire.persistence.dto.OrganizationDTO;
import es.gob.fire.persistence.dto.SignatureDTO;
import es.gob.fire.persistence.entity.Signature;

/** 
 * <p>Interface that provides communication with the operations of the persistence layer.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.0, 14/04/2020.
 */
public interface ISignatureService {

	/**
     * Method that obtains the information for a signature by its identifier.
     * 
     * @param signatureId The signature identifier.
     * @return {@link Signature}
     */
    Signature getSignatureBySignatureId(Long signatureId);

    /**
     * Method that obtains from the persistence a signatures list grouped by application 
     * and filtered by its year and month.
     * 
     * @param month Month (1-12)
     * @param year Year (AAAA)
     * @return Object list that represents the signatures from the persistence.
     */
    List<SignatureDTO> getSignaturesByApplication(Integer month, Integer year);

    /**
     * Method that obtains from the persistence a signatures list grouped by application 
     * and filtered by its year and month, applying optional filters on applications and organizations.
     * 
     * @param month Month (1-12)
     * @param year Year (AAAA)
     * @param applications List of application names to filter or null.
     * @param organizations List of organization names to filter or null.
     * @return Object list that represents the signatures from the persistence.
     */
    List<SignatureDTO> getSignaturesByApplication(Integer month, Integer year, List<String> applications, List<String> organizations);

    /**
     * Method that obtains from the persistence a signatures list grouped by provider 
     * and filtered by its year and month.
     * 
     * @param month Month (1-12)
     * @param year Year (AAAA)
     * @return Object list that represents the signatures from the persistence.
     */
    List<SignatureDTO> getSignaturesByProvider(Integer month, Integer year);

    /**
     * Method that obtains from the persistence a signatures list grouped by provider 
     * and filtered by its year and month, applying optional filters on applications and organizations.
     * 
     * @param month Month (1-12)
     * @param year Year (AAAA)
     * @param applications List of application names to filter or null.
     * @param organizations List of organization names to filter or null.
     * @return Object list that represents the signatures from the persistence.
     */
    List<SignatureDTO> getSignaturesByProvider(Integer month, Integer year, List<String> applications, List<String> organizations);

    /**
     * Method that obtains from the persistence a signatures list grouped by format 
     * and filtered by its year and month.
     * 
     * @param month Month (1-12)
     * @param year Year (AAAA)
     * @return Object list that represents the signatures from the persistence.
     */
    List<SignatureDTO> getSignaturesByFormat(Integer month, Integer year);

    /**
     * Method that obtains from the persistence a signatures list grouped by format 
     * and filtered by its year and month, applying optional filters on applications and organizations.
     * 
     * @param month Month (1-12)
     * @param year Year (AAAA)
     * @param applications List of application names to filter or null.
     * @param organizations List of organization names to filter or null.
     * @return Object list that represents the signatures from the persistence.
     */
    List<SignatureDTO> getSignaturesByFormat(Integer month, Integer year, List<String> applications, List<String> organizations);

    /**
     * Method that obtains from the persistence a signatures list grouped by improved format 
     * (long live format) and filtered by its year and month.
     * 
     * @param month Month (1-12)
     * @param year Year (AAAA)
     * @return Object list that represents the signatures from the persistence.
     */
    List<SignatureDTO> getSignaturesByImprovedFormat(Integer month, Integer year);

    /**
     * Method that obtains from the persistence a signatures list grouped by improved format 
     * (long live format) and filtered by its year and month, applying optional filters on applications and organizations.
     * 
     * @param month Month (1-12)
     * @param year Year (AAAA)
     * @param applications List of application names to filter or null.
     * @param organizations List of organization names to filter or null.
     * @return Object list that represents the signatures from the persistence.
     */
    List<SignatureDTO> getSignaturesByImprovedFormat(Integer month, Integer year, List<String> applications, List<String> organizations);

    /**
     * Method that obtains from the persistence a signatures list grouped by organism 
     * and filtered by its year and month.
     * 
     * @param month Month (1-12)
     * @param year Year (AAAA)
     * @return Object list that represents the signatures from the persistence.
     */
    List<SignatureDTO> getSignaturesByOrganism(Integer month, Integer year);

    /**
     * Method that obtains from the persistence a signatures list grouped by organism 
     * and filtered by its year and month, applying optional filters on applications and organizations.
     * 
     * @param month Month (1-12)
     * @param year Year (AAAA)
     * @param applications List of application names to filter or null.
     * @param organizations List of organization names to filter or null.
     * @return Object list that represents the signatures from the persistence.
     */
    List<SignatureDTO> getSignaturesByOrganism(Integer month, Integer year, List<String> applications, List<String> organizations);

    // METHODS WITH INTERVAL (startMonth/startYear - endMonth/endYear)

    /**
     * Retrieves a list of signatures grouped by application, filtered by a date range defined by the provided start and end month/year.
     * 
     * @param startMonth the starting month (1–12) of the date range.
     * @param startYear the starting year of the date range.
     * @param endMonth the ending month (1–12) of the date range.
     * @param endYear the ending year of the date range.
     * @return a list of {@link SignatureDTO} objects matching the specified date range.
     */
    List<SignatureDTO> getSignaturesByApplication(Integer startMonth, Integer startYear, Integer endMonth, Integer endYear);

    /**
     * Retrieves a list of signatures grouped by application, filtered by a date range defined by the provided start and end month/year,
     * and applying optional filters on applications and organizations.
     * 
     * @param startMonth the starting month (1–12) of the date range.
     * @param startYear the starting year of the date range.
     * @param endMonth the ending month (1–12) of the date range.
     * @param endYear the ending year of the date range.
     * @param applications List of application names to filter or null.
     * @param organizations List of organization names to filter or null.
     * @return a list of {@link SignatureDTO} objects matching the specified date range.
     */
    List<SignatureDTO> getSignaturesByApplication(Integer startMonth, Integer startYear, Integer endMonth, Integer endYear, List<String> applications, List<String> organizations);

    /**
     * Retrieves a list of signatures grouped by provider, filtered by a date range defined by the provided start and end month/year.
     * 
     * @param startMonth the starting month (1–12) of the date range.
     * @param startYear the starting year of the date range.
     * @param endMonth the ending month (1–12) of the date range.
     * @param endYear the ending year of the date range.
     * @return a list of {@link SignatureDTO} objects matching the specified date range.
     */
    List<SignatureDTO> getSignaturesByProvider(Integer startMonth, Integer startYear, Integer endMonth, Integer endYear);

    /**
     * Retrieves a list of signatures grouped by provider, filtered by a date range defined by the provided start and end month/year,
     * and applying optional filters on applications and organizations.
     * 
     * @param startMonth the starting month (1–12) of the date range.
     * @param startYear the starting year of the date range.
     * @param endMonth the ending month (1–12) of the date range.
     * @param endYear the ending year of the date range.
     * @param applications List of application names to filter or null.
     * @param organizations List of organization names to filter or null.
     * @return a list of {@link SignatureDTO} objects matching the specified date range.
     */
    List<SignatureDTO> getSignaturesByProvider(Integer startMonth, Integer startYear, Integer endMonth, Integer endYear, List<String> applications, List<String> organizations);

    /**
     * Retrieves a list of signatures grouped by format, filtered by a date range defined by the provided start and end month/year.
     * 
     * @param startMonth the starting month (1–12) of the date range.
     * @param startYear the starting year of the date range.
     * @param endMonth the ending month (1–12) of the date range.
     * @param endYear the ending year of the date range.
     * @return a list of {@link SignatureDTO} objects matching the specified date range.
     */
    List<SignatureDTO> getSignaturesByFormat(Integer startMonth, Integer startYear, Integer endMonth, Integer endYear);

    /**
     * Retrieves a list of signatures grouped by format, filtered by a date range defined by the provided start and end month/year,
     * and applying optional filters on applications and organizations.
     * 
     * @param startMonth the starting month (1–12) of the date range.
     * @param startYear the starting year of the date range.
     * @param endMonth the ending month (1–12) of the date range.
     * @param endYear the ending year of the date range.
     * @param applications List of application names to filter or null.
     * @param organizations List of organization names to filter or null.
     * @return a list of {@link SignatureDTO} objects matching the specified date range.
     */
    List<SignatureDTO> getSignaturesByFormat(Integer startMonth, Integer startYear, Integer endMonth, Integer endYear, List<String> applications, List<String> organizations);

    /**
     * Retrieves a list of signatures grouped by improved format, filtered by a date range defined by the provided start and end month/year.
     * 
     * @param startMonth the starting month (1–12) of the date range.
     * @param startYear the starting year of the date range.
     * @param endMonth the ending month (1–12) of the date range.
     * @param endYear the ending year of the date range.
     * @return a list of {@link SignatureDTO} objects matching the specified date range.
     */
    List<SignatureDTO> getSignaturesByImprovedFormat(Integer startMonth, Integer startYear, Integer endMonth, Integer endYear);

    /**
     * Retrieves a list of signatures grouped by improved format, filtered by a date range defined by the provided start and end month/year,
     * and applying optional filters on applications and organizations.
     * 
     * @param startMonth the starting month (1–12) of the date range.
     * @param startYear the starting year of the date range.
     * @param endMonth the ending month (1–12) of the date range.
     * @param endYear the ending year of the date range.
     * @param applications List of application names to filter or null.
     * @param organizations List of organization names to filter or null.
     * @return a list of {@link SignatureDTO} objects matching the specified date range.
     */
    List<SignatureDTO> getSignaturesByImprovedFormat(Integer startMonth, Integer startYear, Integer endMonth, Integer endYear, List<String> applications, List<String> organizations);

    /**
     * Retrieves a list of signatures grouped by organism, filtered by a date range defined by the provided start and end month/year.
     * 
     * @param startMonth the starting month (1–12) of the date range.
     * @param startYear the starting year of the date range.
     * @param endMonth the ending month (1–12) of the date range.
     * @param endYear the ending year of the date range.
     * @return a list of {@link SignatureDTO} objects matching the specified date range.
     */
    List<SignatureDTO> getSignaturesByOrganism(Integer startMonth, Integer startYear, Integer endMonth, Integer endYear);

    /**
     * Retrieves a list of signatures grouped by organism, filtered by a date range defined by the provided start and end month/year,
     * and applying optional filters on applications and organizations.
     * 
     * @param startMonth the starting month (1–12) of the date range.
     * @param startYear the starting year of the date range.
     * @param endMonth the ending month (1–12) of the date range.
     * @param endYear the ending year of the date range.
     * @param applications List of application names to filter or null.
     * @param organizations List of organization names to filter or null.
     * @return a list of {@link SignatureDTO} objects matching the specified date range.
     */
    List<SignatureDTO> getSignaturesByOrganism(Integer startMonth, Integer startYear, Integer endMonth, Integer endYear, List<String> applications, List<String> organizations);

    List<String> getDifferentApplications();
    
    List<OrganizationDTO> getDifferentOrganizations();
}

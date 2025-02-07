/*
/*******************************************************************************
 * Copyright (C) 2018 MINHAFP, Gobierno de España
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
 * Provides scheduling and task execution functionalities using Quartz.
 * 
 * <p>This package contains classes and configurations related to task scheduling
 * using the Quartz framework. It allows for the execution of scheduled tasks
 * at predefined intervals or based on specific triggers.</p>
 * 
 * <h2>Key Features:</h2>
 * <ul>
 *   <li>Task scheduling with different planner types (daily, periodic, etc.).</li>
 *   <li>Integration with the system's persistence layer to retrieve and update task configurations.</li>
 *   <li>Support for enabling/disabling scheduled tasks dynamically.</li>
 *   <li>Execution of tasks based on time-based triggers.</li>
 * </ul>
 * 
 * <h2>Usage:</h2>
 * <p>To use this package, define and configure a scheduler with the required execution parameters.
 * The tasks will be executed automatically based on the assigned triggers.</p>
 * 
 * <h2>License:</h2>
 * <p>This package is part of a software licensed under the European Public License (EUPL).</p>
 */
package es.gob.fire.quartz;

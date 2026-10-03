import { In } from 'typeorm';
import { Application } from '../../domain/apply/application';
import { dueNotificationOf } from '../../domain/apply/application-result-log';
import { NotFoundError } from '../../support/errors';
import { ApplicationService } from '../../support/stereotype';
import { InterviewTimeFinder } from '../recruitment/provided/interview-time-finder';
import { apiId } from '../shared/api-id';
import { formatDate } from '../shared/dates';
import { PageResponse, pageResponse } from '../shared/page';
import { ApplicationPhotoService } from './application-photo.service';
import { ApplicationFinder } from './provided/application-finder';
import { FormsQuery } from './provided/apply-requests';
import {
  ApplicationDetailResponse,
  ApplicationSummaryResponse,
} from './provided/apply-responses';
import { ApplicationInterviewTimeRepository } from './required/application-interview-time-repository';
import { ApplicationRepository } from './required/application-repository';
import { ApplicationResultLogRepository } from './required/application-result-log-repository';
import { ClubActivityRepository } from './required/club-activity-repository';

@ApplicationService()
export class ApplicationQueryService implements ApplicationFinder {
  constructor(
    private readonly applications: ApplicationRepository,
    private readonly activities: ClubActivityRepository,
    private readonly interviewChoices: ApplicationInterviewTimeRepository,
    private readonly resultLogs: ApplicationResultLogRepository,
    private readonly interviewTimes: InterviewTimeFinder,
    private readonly photos: ApplicationPhotoService,
  ) {}

  async getById(id: string): Promise<Application> {
    const application = await this.applications.findOneBy({ id });
    if (!application)
      throw new NotFoundError('해당 지원서가 존재하지 않습니다.');
    return application;
  }
  /** A null decision matches IS NULL, so undecided applications can be selected. */
  private decided(documentPass: boolean | null, finalPass: boolean | null) {
    const builder = this.applications.createQueryBuilder('application');
    if (documentPass === null)
      builder.andWhere('application.document_pass IS NULL');
    else
      builder.andWhere('application.document_pass = :documentPass', {
        documentPass: documentPass ? 1 : 0,
      });
    if (finalPass === null) builder.andWhere('application.final_pass IS NULL');
    else
      builder.andWhere('application.final_pass = :finalPass', {
        finalPass: finalPass ? 1 : 0,
      });
    return builder;
  }
  /** Every semester's applications with these decisions, in ID order. */
  async findByDecision(
    documentPass: boolean,
    finalPass: boolean | null,
  ): Promise<Application[]> {
    return this.decided(documentPass, finalPass)
      .orderBy('application.id', 'ASC')
      .getMany();
  }
  async list(
    query: FormsQuery,
  ): Promise<PageResponse<ApplicationSummaryResponse>> {
    const [applications, total] = await this.decided(
      query.documentPass,
      query.finalPass,
    )
      .andWhere('application.semester = :semester', {
        semester: query.semester,
      })
      .orderBy('application.interview_time', 'ASC')
      .addOrderBy('application.id', 'ASC')
      .skip(query.page * query.size)
      .take(query.size)
      .getManyAndCount();
    const logs = applications.length
      ? await this.resultLogs.findBy({
          applicationId: In(applications.map((application) => application.id)),
        })
      : [];
    return pageResponse(
      applications.map((application) => {
        const due = dueNotificationOf(
          application.documentPass,
          application.finalPass,
        );
        const sent = logs.find(
          (log) =>
            log.applicationId === application.id &&
            log.notificationType === due,
        );
        return {
          id: apiId(application.id),
          photo: this.photos.photoUrl(application.photoKey, application.photo),
          name: application.name,
          interviewTime: formatDate(application.interviewTime, 'monthTime'),
          documentPass: application.documentPass,
          finalPass: application.finalPass,
          mailSentAt: formatDate(sent?.sentAt ?? null, 'interview'),
        };
      }),
      total,
      query,
    );
  }
  async detail(id: string): Promise<ApplicationDetailResponse> {
    const application = await this.getById(id);
    const activities = await this.activities.find({
      where: { applicationId: id },
      order: { id: 'ASC' },
    });
    const choices = await this.interviewChoices.findBy({ applicationId: id });
    const times = await this.interviewTimes.findByIds(
      choices.map((choice) => choice.interviewTimeId),
    );
    const { photoKey, ...fields } = application;
    return {
      ...fields,
      id: apiId(id),
      studentId: apiId(application.studentId),
      semester:
        application.semester === null ? null : apiId(application.semester),
      photo: this.photos.photoUrl(photoKey, application.photo),
      submitTime: formatDate(application.submitTime, 'monthTime'),
      interviewTime: formatDate(application.interviewTime, 'monthTime'),
      activities: activities.map(({ clubName, startDate, endDate, role }) => ({
        clubName,
        startDate,
        endDate,
        role,
      })),
      availableInterviewTimes: times,
    };
  }
}
